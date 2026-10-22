package main

import (
	"context"
	"crypto/rand"
	"errors"
	"math/big"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

// DB wraps the pgx pool. All queries are plain SQL matching ../schema.sql.
type DB struct {
	pool *pgxpool.Pool
}

func NewDB(url string) (*DB, error) {
	pool, err := pgxpool.New(context.Background(), url)
	if err != nil {
		return nil, err
	}
	if err := pool.Ping(context.Background()); err != nil {
		return nil, err
	}
	return &DB{pool: pool}, nil
}

func (d *DB) Close() { d.pool.Close() }

// ── auth ────────────────────────────────────────────────────────────────────

// UpsertGoogleUser creates or updates the user by Google `sub` and returns
// (userID, role, pseudoID).
func (d *DB) UpsertGoogleUser(ctx context.Context, sub, email, name string) (string, string, string, error) {
	const q = `
		WITH u AS (
			INSERT INTO users (google_sub, email, display_name)
			VALUES ($1, $2, $3)
			ON CONFLICT (google_sub) DO UPDATE SET email = EXCLUDED.email
			RETURNING id, role
		)
		SELECT u.id::text, u.role,
		       COALESCE(pp.pseudo_id, '')
		FROM u
		LEFT JOIN patient_profiles pp ON pp.user_id = u.id`
	var userID, role, pseudoID string
	err := d.pool.QueryRow(ctx, q, sub, email, name).Scan(&userID, &role, &pseudoID)
	return userID, role, pseudoID, err
}

// ── pairing ─────────────────────────────────────────────────────────────────

// LinkPairingCode binds the patient to the code's doctor. The UNIQUE
// (patient_id) constraint in doctor_patients enforces 1 doctor per patient.
func (d *DB) LinkPairingCode(ctx context.Context, userID, code string) (doctorName string, err error) {
	tx, err := d.pool.Begin(ctx)
	if err != nil {
		return "", err
	}
	defer tx.Rollback(ctx)

	var role string
	var codeID, doctorID string
	var usedBy *string
	var expiresAt time.Time

	err = tx.QueryRow(ctx, `SELECT role FROM users WHERE id = $1`, userID).Scan(&role)
	if err != nil {
		return "", err
	}
	if role != "patient" {
		return "", errors.New("only patients can link a pairing code")
	}

	err = tx.QueryRow(ctx, `
		SELECT id::text, doctor_id::text, used_by::text, expires_at
		FROM pairing_codes
		WHERE code = $1
		FOR UPDATE`, code).Scan(&codeID, &doctorID, &usedBy, &expiresAt)
	if errors.Is(err, pgx.ErrNoRows) {
		return "", errNotFound
	}
	if err != nil {
		return "", err
	}
	if usedBy != nil {
		return "", errors.New("code already used")
	}
	if time.Now().After(expiresAt) {
		return "", errors.New("code expired")
	}

	// Hard stop if the patient already has an active doctor.
	var existing string
	err = tx.QueryRow(ctx, `
		SELECT doctor_id::text FROM doctor_patients
		WHERE patient_id = $1 AND active`, userID).Scan(&existing)
	if err == nil {
		return "", errors.New("patient already linked to a doctor")
	}
	if !errors.Is(err, pgx.ErrNoRows) {
		return "", err
	}

	if _, err = tx.Exec(ctx, `
		INSERT INTO doctor_patients (doctor_id, patient_id)
		VALUES ($1, $2)`, doctorID, userID); err != nil {
		return "", err
	}
	if _, err = tx.Exec(ctx, `
		UPDATE patient_profiles SET doctor_user_id = $2, linked_at = now()
		WHERE user_id = $1`, userID, doctorID); err != nil {
		return "", err
	}
	if _, err = tx.Exec(ctx, `
		UPDATE pairing_codes SET used_by = $2, used_at = now() WHERE id = $1`,
		codeID, userID); err != nil {
		return "", err
	}

	err = tx.QueryRow(ctx, `SELECT display_name FROM users WHERE id = $1`, doctorID).Scan(&doctorName)
	if err != nil {
		return "", err
	}
	return doctorName, tx.Commit(ctx)
}

func (d *DB) PairingStatus(ctx context.Context, userID string) (doctorName string, linked bool, err error) {
	const q = `
		SELECT u.display_name
		FROM patient_profiles pp
		JOIN users u ON u.id = pp.doctor_user_id
		WHERE pp.user_id = $1`
	err = d.pool.QueryRow(ctx, q, userID).Scan(&doctorName)
	if errors.Is(err, pgx.ErrNoRows) {
		return "", false, nil
	}
	return doctorName, err == nil, err
}

func (d *DB) UnlinkDoctor(ctx context.Context, userID string) error {
	const q = `
		UPDATE doctor_patients SET active = FALSE
		WHERE patient_id = $1 AND active`
	_, err := d.pool.Exec(ctx, q, userID)
	return err
}

const codeAlphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789" // no I/L/O/0/1

// CreatePairingCode issues a new single-use 8-char code for this doctor.
func (d *DB) CreatePairingCode(ctx context.Context, doctorID string) (string, error) {
	for attempt := 0; attempt < 5; attempt++ {
		code := make([]byte, 8)
		for i := range code {
			n, err := rand.Int(rand.Reader, big.NewInt(int64(len(codeAlphabet))))
			if err != nil {
				return "", err
			}
			code[i] = codeAlphabet[n.Int64()]
		}
		_, err := d.pool.Exec(ctx, `
			INSERT INTO pairing_codes (code, doctor_id) VALUES ($1, $2)`, string(code), doctorID)
		if err != nil {
			continue // unique collision → draw again
		}
		return string(code), nil
	}
	return "", errors.New("could not allocate a unique code")
}

// ── uploads (dumb store) ────────────────────────────────────────────────────

// InsertUpload stores the raw payload. Retries with the same idempotency key
// are no-ops (returns inserted=false).
func (d *DB) InsertUpload(ctx context.Context, patientID, idemKey, schemaVersion, kind string, payload []byte, from, to *time.Time) (bool, error) {
	tag, err := d.pool.Exec(ctx, `
		INSERT INTO data_uploads (patient_id, idempotency_key, schema_version, kind, payload, payload_from, payload_to)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		ON CONFLICT (idempotency_key) DO NOTHING`,
		patientID, idemKey, schemaVersion, kind, payload, from, to)
	if err != nil {
		return false, err
	}
	return tag.RowsAffected() == 1, nil
}

// DoctorPatientUploads verifies the relation then returns stored payloads,
// writing an audit row — doctors reading patient data must leave a trace.
func (d *DB) DoctorPatientUploads(ctx context.Context, doctorID, patientID string, limit int) ([][]byte, error) {
	var ok bool
	err := d.pool.QueryRow(ctx, `
		SELECT EXISTS (
			SELECT 1 FROM doctor_patients
			WHERE doctor_id = $1 AND patient_id = $2 AND active)`,
		doctorID, patientID).Scan(&ok)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, errNotFound
	}

	if limit <= 0 || limit > 365 {
		limit = 100
	}
	rows, err := d.pool.Query(ctx, `
		SELECT payload FROM data_uploads
		WHERE patient_id = $1
		ORDER BY payload_from DESC NULLS LAST
		LIMIT $2`, patientID, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var out [][]byte
	for rows.Next() {
		var payload []byte
		if err := rows.Scan(&payload); err != nil {
			return nil, err
		}
		out = append(out, payload)
	}
	if _, err = d.pool.Exec(ctx, `
		INSERT INTO audit_log (actor_id, action, target_id, detail)
		VALUES ($1, 'READ_PATIENT_UPLOADS', $2, '{"via":"api"}'::jsonb)`,
		doctorID, patientID); err != nil {
		return out, err
	}
	return out, nil
}

// DoctorPatients lists the doctor's active patients (pseudo IDs only).
func (d *DB) DoctorPatients(ctx context.Context, doctorID string) ([]map[string]any, error) {
	rows, err := d.pool.Query(ctx, `
		SELECT u.id::text, pp.pseudo_id, dp.linked_at
		FROM doctor_patients dp
		JOIN users u ON u.id = dp.patient_id
		JOIN patient_profiles pp ON pp.user_id = dp.patient_id
		WHERE dp.doctor_id = $1 AND dp.active
		ORDER BY dp.linked_at DESC`, doctorID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var out []map[string]any
	for rows.Next() {
		var id, pseudo string
		var linked time.Time
		if err := rows.Scan(&id, &pseudo, &linked); err != nil {
			return nil, err
		}
		out = append(out, map[string]any{
			"patient_id": id, "pseudo_id": pseudo, "linked_at": linked,
		})
	}
	return out, rows.Err()
}
