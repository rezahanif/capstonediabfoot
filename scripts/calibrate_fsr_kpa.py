#!/usr/bin/env python3
"""
DiabFoot Guard — FSR ADC to Pressure (kPa) Calibration & Curve Fit Benchmark
Calculates exact mathematical constants from empirical weight data.
"""

import numpy as np

# Empirical calibration data provided by user
DATA = [
    # ADC_Analitik, Weight_kg, Pressure_kPa
    (0,    0,    0),
    (893,  2,    444),
    (1825, 4,    888),
    (1985, 6,    1332),
    (2263, 8,    1776),
    (2458, 10,   2220),
    (2587, 12,   2664),
    (2716, 14,   3108),
    (2844, 16,   3552),
    (2973, 18,   3996),
    (3102, 20,   4440),
]

adc = np.array([d[0] for d in DATA], dtype=np.float64)
kpa = np.array([d[2] for d in DATA], dtype=np.float64)

print("=" * 80)
print("  FSR ADC -> PRESSURE (kPa) RIGOROUS CALIBRATION BENCHMARK")
print("=" * 80)

# 1. Polynomial Fits (Deg 2, 3, 4)
fits = {}
for deg in [2, 3, 4]:
    coeffs = np.polyfit(adc, kpa, deg)
    predicted = np.polyval(coeffs, adc)
    rmse = np.sqrt(np.mean((kpa - predicted) ** 2))
    max_err = np.max(np.abs(kpa - predicted))
    r2 = 1.0 - (np.sum((kpa - predicted) ** 2) / np.sum((kpa - np.mean(kpa)) ** 2))
    fits[deg] = {
        "coeffs": coeffs,
        "rmse": rmse,
        "max_err": max_err,
        "r2": r2,
        "pred": predicted
    }
    print(f"\n[+] Polynomial Degree {deg}:")
    print(f"    R^2 Correlation : {r2:.6f}")
    print(f"    RMSE Error      : {rmse:.2f} kPa")
    print(f"    Max Error       : {max_err:.2f} kPa")
    print("    Exact Coefficients (highest power to lowest):")
    for i, c in enumerate(coeffs):
        power = deg - i
        print(f"      a{power} = {c:+.10e}")

# 2. Piecewise Linear Slopes Calculation (m_i and b_i per interval)
print("\n" + "=" * 80)
print("  PIECEWISE LINEAR LOOKUP TABLE (LUT) COEFFICIENTS")
print("=" * 80)
print("Interval i | ADC Range     | Slope m_i (kPa/ADC) | Offset b_i (kPa) | Max Error")
print("-" * 80)
pw_segments = []
for i in range(len(DATA) - 1):
    x0, y0 = adc[i], kpa[i]
    x1, y1 = adc[i+1], kpa[i+1]
    dx = x1 - x0
    dy = y1 - y0
    slope = dy / dx
    offset = y0 - slope * x0
    pw_segments.append((x0, x1, slope, offset, y0, y1))
    print(f"Segment {i:2d} | [{int(x0):4d} .. {int(x1):4d}] | {slope:17.6f} | {offset:16.4f} |   0.00 kPa (Exact)")

# 3. Residual Error Comparison Table on All 11 Ground-Truth Points
print("\n" + "=" * 80)
print("  POINT-BY-POINT COMPARISON: Ground Truth vs Poly Deg 4 vs Piecewise")
print("=" * 80)
print("ADC    | Actual kPa | Poly4 Pred (kPa) | Poly4 Err (kPa) | Poly4 Err % | Piecewise Err")
print("-" * 80)
p4_coeffs = fits[4]["coeffs"]
p4_pred = fits[4]["pred"]
for i in range(len(DATA)):
    a = int(adc[i])
    act = kpa[i]
    p4 = p4_pred[i]
    err = p4 - act
    pct = (err / act * 100.0) if act > 0 else 0.0
    print(f"{a:5d}  | {act:10.1f} | {p4:16.1f} | {err:+14.1f} | {pct:+10.2f}% |      0.00 kPa")

print("=" * 80)
