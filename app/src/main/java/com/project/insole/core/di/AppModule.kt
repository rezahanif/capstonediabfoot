package com.project.insole.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.project.insole.core.ble.InsoleBleManager
import com.project.insole.core.ble.BleConnectionManager
import com.project.insole.core.database.InsoleDatabase
import com.project.insole.core.database.dao.DailyFootMetricDao
import com.project.insole.core.database.dao.HourlyStepDao
import com.project.insole.core.database.dao.NotificationDao
import com.project.insole.core.database.dao.StepDao
import com.project.insole.core.notifications.InsoleNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for app-wide singleton dependencies.
 * Include BLE manager, network client, database instances here.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBleManager(@ApplicationContext context: Context): InsoleBleManager {
        return InsoleBleManager(context)
    }

    @Provides
    @Singleton
    fun provideBleConnectionManager(
        @ApplicationContext context: Context,
        bleManager: InsoleBleManager
    ): BleConnectionManager {
        return BleConnectionManager(context, bleManager)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): InsoleNotificationManager {
        return InsoleNotificationManager(context)
    }

    /**
     * v2→v3: adds the per-foot step columns. A real migration (not
     * fallbackToDestructiveMigration) because wiping historical step data is
     * unacceptable for a medical monitoring app.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE steps ADD COLUMN leftCount INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE steps ADD COLUMN rightCount INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * v3→v4: adds the daily_foot_metrics table for TD-7 (thermal summaries,
     * peak pressure, and wear minutes persistence). Real migration preserves
     * step history.
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `daily_foot_metrics` (
                    `date` TEXT NOT NULL,
                    `leftMinTemp` REAL NOT NULL DEFAULT 0.0,
                    `leftMaxTemp` REAL NOT NULL DEFAULT 0.0,
                    `leftSumTemp` REAL NOT NULL DEFAULT 0.0,
                    `leftSamples` INTEGER NOT NULL DEFAULT 0,
                    `rightMinTemp` REAL NOT NULL DEFAULT 0.0,
                    `rightMaxTemp` REAL NOT NULL DEFAULT 0.0,
                    `rightSumTemp` REAL NOT NULL DEFAULT 0.0,
                    `rightSamples` INTEGER NOT NULL DEFAULT 0,
                    `leftPeakPressureKpa` REAL NOT NULL DEFAULT 0.0,
                    `rightPeakPressureKpa` REAL NOT NULL DEFAULT 0.0,
                    `wearMinutes` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`date`)
                )
            """.trimIndent())
        }
    }

    /**
     * v4→v5: adds the notifications table for local clinical alert and guidance history.
     */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `notifications` (
                    `id` TEXT NOT NULL,
                    `promptId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `message` TEXT NOT NULL,
                    `severity` TEXT NOT NULL,
                    `foot` TEXT,
                    `timestamp` INTEGER NOT NULL,
                    `thresholdVersion` TEXT NOT NULL DEFAULT 'v1',
                    `read` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`)
                )
            """.trimIndent())
        }
    }

    /**
     * v5→v6: adds clean data metrics and 2-day consecutive hotspot tracking
     * to daily_foot_metrics for noise-immune clinical scoring.
     */
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanLeftMinTemp REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanLeftMaxTemp REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanLeftSumTemp REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanLeftSamples INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanRightMinTemp REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanRightMaxTemp REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanRightSumTemp REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanRightSamples INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanWearMinutes INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanStancePressureSumKpa REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN cleanStanceSamples INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN consecutiveThermalDays INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE daily_foot_metrics ADD COLUMN sustainedThermalSide TEXT DEFAULT NULL")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InsoleDatabase {
        return Room.databaseBuilder(
            context,
            InsoleDatabase::class.java,
            "insole_db"
        )
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
        .build()
    }

    @Provides
    @Singleton
    fun provideStepDao(database: InsoleDatabase): StepDao {
        return database.stepDao()
    }

    @Provides
    @Singleton
    fun provideHourlyStepDao(database: InsoleDatabase): HourlyStepDao {
        return database.hourlyStepDao()
    }

    @Provides
    @Singleton
    fun provideDailyFootMetricDao(database: InsoleDatabase): DailyFootMetricDao {
        return database.dailyFootMetricDao()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: InsoleDatabase): NotificationDao {
        return database.notificationDao()
    }
}
