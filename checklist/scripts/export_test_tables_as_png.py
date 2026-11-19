#!/usr/bin/env python3
"""
Export TEST-06 and TEST-07 empirical benchmark tables directly as high-resolution PNG images.
Uses ReportLab vector table layout + pdftoppm 300 DPI anti-aliased rendering.
"""

import os
import subprocess
import shutil
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, HRFlowable
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

OUTPUT_DIR = "checklist/results/figures"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Brand Colors
PRIMARY = colors.HexColor("#006684")       # Deep Medical Ocean Blue
PRIMARY_DARK = colors.HexColor("#001F2A")  # Midnight Navy
ACCENT = colors.HexColor("#00A3C4")        # Cyan Blue
PASS_GREEN = colors.HexColor("#1B6E32")    # Forest Green
PASS_BG = colors.HexColor("#E8F5E9")       # Soft Green
TEXT_DARK = colors.HexColor("#1E293B")     # Slate 800
TEXT_MUTED = colors.HexColor("#64748B")    # Slate 500
BORDER_COLOR = colors.HexColor("#CBD5E1")  # Slate 300
ROW_BG_ALT = colors.HexColor("#F8FAFC")    # Slate 50

styles = getSampleStyleSheet()

title_style = ParagraphStyle(
    'TableTitle',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=13,
    leading=16,
    textColor=PRIMARY_DARK,
    spaceAfter=3
)
subtitle_style = ParagraphStyle(
    'TableSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8.5,
    leading=11,
    textColor=TEXT_MUTED,
    spaceAfter=8
)
table_cell = ParagraphStyle(
    'TableCell',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8.5,
    leading=11.5,
    textColor=TEXT_DARK
)
table_cell_bold = ParagraphStyle(
    'TableCellBold',
    parent=table_cell,
    fontName='Helvetica-Bold'
)
table_cell_center = ParagraphStyle(
    'TableCellCenter',
    parent=table_cell,
    alignment=1
)
table_cell_pass = ParagraphStyle(
    'TableCellPass',
    parent=table_cell,
    fontName='Helvetica-Bold',
    textColor=PASS_GREEN,
    alignment=1
)
table_header = ParagraphStyle(
    'TableHeader',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=8.5,
    leading=11.5,
    textColor=colors.white,
    alignment=1
)

def render_table_pdf_to_png(pdf_path, png_final_path, page_size, story):
    doc = SimpleDocTemplate(
        pdf_path,
        pagesize=page_size,
        leftMargin=14,
        rightMargin=14,
        topMargin=14,
        bottomMargin=14
    )
    doc.build(story)
    
    # Render with pdftoppm at 300 DPI
    prefix = pdf_path.replace(".pdf", "_ppm")
    cmd = ["pdftoppm", "-png", "-r", "300", pdf_path, prefix]
    subprocess.run(cmd, check=True)
    
    generated_png = f"{prefix}-1.png"
    if os.path.exists(generated_png):
        shutil.move(generated_png, png_final_path)
        print(f"Successfully generated table PNG: {png_final_path}")
    if os.path.exists(pdf_path):
        os.remove(pdf_path)


def export_test06_table():
    pdf_path = "/tmp/test06_table_temp.pdf"
    png_path = os.path.join(OUTPUT_DIR, "test_06_table.png")
    
    story = [
        Paragraph("TEST-06: BLE Communication Packet Loss & Transport Reliability", title_style),
        Paragraph("Target: Smart_Insole_Left | Duration: 60s Active Stream | RSSI: -66 dBm | Protocol: GATT Batching", subtitle_style),
        Table([
            [Paragraph("Metric", table_header), Paragraph("Empirical Value", table_header), Paragraph("Requirement", table_header), Paragraph("Verdict", table_header)],
            [Paragraph("Total Transmitted Telemetry Samples", table_cell), Paragraph("<b>223 samples</b>", table_cell_center), Paragraph("&ge; 100 samples", table_cell), Paragraph("Sufficient", table_cell_center)],
            [Paragraph("Total Valid Received Samples", table_cell), Paragraph("<b>223 samples</b>", table_cell_center), Paragraph("Monotonic sequence", table_cell), Paragraph("Verified", table_cell_center)],
            [Paragraph("Dropped / Lost Packets", table_cell), Paragraph("<b>0 packets</b>", table_cell_pass), Paragraph("Minimal", table_cell), Paragraph("Zero Drops", table_cell_pass)],
            [Paragraph("Packet Loss Rate (%)", table_cell_bold), Paragraph("<b>0.00%</b>", table_cell_pass), Paragraph("Target &lt; 10.0%", table_cell), Paragraph("<b>PASSED</b>", table_cell_pass)],
            [Paragraph("CRC16 / Checksum Invalidation Errors", table_cell), Paragraph("<b>0 errors</b>", table_cell_center), Paragraph("0 errors", table_cell), Paragraph("Clean", table_cell_pass)],
            [Paragraph("GATT Batches Delivered", table_cell), Paragraph("<b>51 batches</b> @ 2 Hz", table_cell_center), Paragraph("Spec v1 Batching", table_cell), Paragraph("Confirmed", table_cell_center)],
            [Paragraph("Sequence Continuity Range", table_cell), Paragraph("<b>seq 49 to seq 271</b>", table_cell_center), Paragraph("100% Contiguous", table_cell), Paragraph("Verified", table_cell_center)],
        ], colWidths=[200, 110, 130, 95], style=[
            ('BACKGROUND', (0,0), (-1,0), PRIMARY_DARK),
            ('BOX', (0,0), (-1,-1), 1, BORDER_COLOR),
            ('INNERGRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
            ('TOPPADDING', (0,0), (-1,-1), 3.5),
            ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
            ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, ROW_BG_ALT]),
        ])
    ]
    render_table_pdf_to_png(pdf_path, png_path, (565, 275), story)


def export_test07_table():
    pdf_path = "/tmp/test07_table_temp.pdf"
    png_path = os.path.join(OUTPUT_DIR, "test_07_table.png")
    
    story = [
        Paragraph("TEST-07: BLE Latency & Interval Stability (ESP32 to Android)", title_style),
        Paragraph("Target: ESP32-C3 &harr; Android App | Mode: OTA GATT Notification Stream | Packets Logged: 268", subtitle_style),
        Table([
            [Paragraph("Latency Parameter", table_header), Paragraph("Measured Value", table_header), Paragraph("Target Spec", table_header), Paragraph("Evaluation", table_header)],
            [Paragraph("Total Captured Packets", table_cell), Paragraph("<b>268 packets</b>", table_cell_center), Paragraph("&ge; 100 packets", table_cell), Paragraph("Complete", table_cell_center)],
            [Paragraph("Buffer Overflow Drops", table_cell), Paragraph("<b>0 drops</b>", table_cell_pass), Paragraph("0 drops", table_cell), Paragraph("Zero Drops (Pass)", table_cell_pass)],
            [Paragraph("Mean Inter-Packet Interval (dt)", table_cell), Paragraph("<b>225.5 ms</b>", table_cell_center), Paragraph("200.0 ms (5 Hz)", table_cell), Paragraph("Acceptable", table_cell_center)],
            [Paragraph("Median Interval (P50)", table_cell), Paragraph("<b>224.99 ms</b>", table_cell_center), Paragraph("200.0 ms", table_cell), Paragraph("Stable Cadence", table_cell_center)],
            [Paragraph("95th Percentile Interval (P95)", table_cell), Paragraph("<b>315.11 ms</b>", table_cell_center), Paragraph("&lt; 350.0 ms", table_cell), Paragraph("Within Bounds", table_cell_center)],
            [Paragraph("Mean App Pipeline Processing", table_cell_bold), Paragraph("<b>1.27 ms</b> (1269.2 &mu;s)", table_cell_pass), Paragraph("&lt; 5.0 ms", table_cell), Paragraph("<b>PASSED (3.9x Margin)</b>", table_cell_pass)],
            [Paragraph("95th Percentile Pipeline Latency", table_cell), Paragraph("<b>1.61 ms</b> (1614.0 &mu;s)", table_cell_pass), Paragraph("&lt; 10.0 ms", table_cell), Paragraph("Real-time", table_cell_pass)],
        ], colWidths=[200, 110, 130, 95], style=[
            ('BACKGROUND', (0,0), (-1,0), PRIMARY_DARK),
            ('BOX', (0,0), (-1,-1), 1, BORDER_COLOR),
            ('INNERGRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
            ('TOPPADDING', (0,0), (-1,-1), 3.5),
            ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
            ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, ROW_BG_ALT]),
        ])
    ]
    render_table_pdf_to_png(pdf_path, png_path, (565, 275), story)


def copy_tables_to_test_dirs():
    mapping = {
        "test_06_table.png": "checklist/test_06/results/test_06_table.png",
        "test_07_table.png": "checklist/test_07/results/test_07_table.png",
    }
    for src_name, dst_path in mapping.items():
        src_path = os.path.join(OUTPUT_DIR, src_name)
        if os.path.exists(src_path):
            os.makedirs(os.path.dirname(dst_path), exist_ok=True)
            shutil.copyfile(src_path, dst_path)
            print(f"Copied {src_name} -> {dst_path}")


if __name__ == "__main__":
    export_test06_table()
    export_test07_table()
    copy_tables_to_test_dirs()
    print("All table PNGs exported successfully.")
