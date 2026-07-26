"""
PDF report via reportlab. Real, working document generation — not a
placeholder. Layout: title page, compliance summary with a simple drawn
score bar, building overview, violations by severity, AI suggestions,
and GIS/fire-access section if available.
"""

from datetime import datetime
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import cm
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak
)
from reportlab.graphics.shapes import Drawing, Rect, String

SEVERITY_COLORS = {
    "CRITICAL": colors.HexColor("#ef4444"),
    "MAJOR": colors.HexColor("#f59e0b"),
    "MINOR": colors.HexColor("#eab308"),
}


def _score_bar(score: float, width=400, height=28) -> Drawing:
    d = Drawing(width, height)
    d.add(Rect(0, 0, width, height, fillColor=colors.HexColor("#e5e7eb"), strokeColor=None))
    fill_width = width * (float(score) / 100)
    bar_color = colors.HexColor("#16a34a") if score >= 80 else (
        colors.HexColor("#f59e0b") if score >= 50 else colors.HexColor("#dc2626")
    )
    d.add(Rect(0, 0, fill_width, height, fillColor=bar_color, strokeColor=None))
    d.add(String(width / 2, height / 2 - 4, f"{score}/100", fontSize=11,
                 fillColor=colors.white, textAnchor="middle"))
    return d


def generate_pdf(data: dict, output_path: str):
    doc = SimpleDocTemplate(output_path, pagesize=A4,
                             topMargin=2 * cm, bottomMargin=2 * cm, leftMargin=2 * cm, rightMargin=2 * cm)
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle("TitleCustom", parent=styles["Title"], fontSize=24, spaceAfter=6)
    h2 = styles["Heading2"]
    body = styles["BodyText"]
    small = ParagraphStyle("Small", parent=styles["BodyText"], fontSize=8, textColor=colors.grey)

    project = data["project"]
    flow = []

    # --- Title page ---
    flow.append(Paragraph("AI RERA Auditor", title_style))
    flow.append(Paragraph("From Blueprint to Approval — Powered by Artificial Intelligence.", body))
    flow.append(Spacer(1, 24))
    flow.append(Paragraph(f"Compliance Audit Report: {project.name}", h2))
    flow.append(Paragraph(f"Location: {project.location or 'Not specified'}", body))
    flow.append(Paragraph(f"Generated: {datetime.utcnow().strftime('%Y-%m-%d %H:%M UTC')}", body))
    flow.append(Spacer(1, 20))

    # --- Compliance summary ---
    flow.append(Paragraph("Compliance Summary", h2))
    if data["compliance_score"] is not None:
        flow.append(_score_bar(float(data["compliance_score"])))
        flow.append(Spacer(1, 8))
        flow.append(Paragraph(f"Approval Probability: {data['approval_probability']}%", body))
    else:
        flow.append(Paragraph("No compliance analysis has been run yet for this project.", body))

    counts = {k: len(v) for k, v in data["violations_by_severity"].items()}
    summary_table = Table(
        [["Critical", "Major", "Minor", "Total"],
         [counts["CRITICAL"], counts["MAJOR"], counts["MINOR"], sum(counts.values())]],
        colWidths=[100, 100, 100, 100],
    )
    summary_table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1f2937")),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("ALIGN", (0, 0), (-1, -1), "CENTER"),
        ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
        ("FONTSIZE", (0, 0), (-1, -1), 10),
    ]))
    flow.append(Spacer(1, 12))
    flow.append(summary_table)
    flow.append(Spacer(1, 20))

    # --- Building overview ---
    if data["buildings"]:
        flow.append(Paragraph("Building Overview", h2))
        rows = [["Name", "Type", "Floors", "FAR", "Ground Coverage %"]]
        for b in data["buildings"]:
            rows.append([
                b.name, b.building_type, str(b.num_floors),
                str(b.far_calculated) if b.far_calculated else "—",
                str(b.ground_coverage_pct) if b.ground_coverage_pct else "—",
            ])
        building_table = Table(rows, colWidths=[110, 90, 60, 80, 120])
        building_table.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#374151")),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
            ("FONTSIZE", (0, 0), (-1, -1), 9),
        ]))
        flow.append(building_table)
        flow.append(Spacer(1, 20))

    # --- Violations ---
    flow.append(Paragraph("Violations", h2))
    if not data["violations"]:
        flow.append(Paragraph("No open violations.", body))
    else:
        for severity in ["CRITICAL", "MAJOR", "MINOR"]:
            items = data["violations_by_severity"].get(severity, [])
            if not items:
                continue
            flow.append(Paragraph(f"{severity.title()} ({len(items)})",
                                   ParagraphStyle("SevHead", parent=h2, fontSize=13,
                                                  textColor=SEVERITY_COLORS[severity])))
            rows = [["Rule", "Description", "Detected", "Required"]]
            for v in items:
                rows.append([
                    v.rule.rule_code if v.rule else "—",
                    Paragraph(v.description, small),
                    str(v.detected_value) if v.detected_value is not None else "—",
                    str(v.required_value) if v.required_value is not None else "—",
                ])
            t = Table(rows, colWidths=[90, 230, 60, 60])
            t.setStyle(TableStyle([
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#111827")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("GRID", (0, 0), (-1, -1), 0.5, colors.grey),
                ("FONTSIZE", (0, 0), (-1, -1), 8),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ]))
            flow.append(t)
            flow.append(Spacer(1, 12))

    # --- AI Suggestions ---
    if data["suggestions"]:
        flow.append(PageBreak())
        flow.append(Paragraph("AI Suggestions", h2))
        for s in data["suggestions"]:
            flow.append(Paragraph(f"• {s.suggestion_text}", body))
            flow.append(Spacer(1, 4))

    # --- GIS / Fire Access ---
    site = data["site"]
    if site:
        flow.append(Spacer(1, 16))
        flow.append(Paragraph("Site & Fire Access", h2))
        flow.append(Paragraph(f"Address: {site.geocoded_address or '—'}", body))
        if site.nearest_road_distance_m is not None:
            estimated_note = " (estimated)" if site.nearest_road_width_is_estimated else " (measured)"
            flow.append(Paragraph(
                f"Nearest road: {site.nearest_road_name or 'Unnamed'} — "
                f"{site.nearest_road_distance_m}m away, {site.nearest_road_width_m}m wide{estimated_note}", body
            ))
            flow.append(Paragraph(
                f"Fire tender access: {'Compliant' if site.fire_access_compliant else 'Non-compliant'}", body
            ))
        if site.encroachment_status == "NOT_AVAILABLE":
            flow.append(Paragraph(
                "Encroachment check: not available (requires an authoritative cadastral/survey boundary).", small
            ))

    # --- Disclaimer ---
    flow.append(Spacer(1, 30))
    flow.append(Paragraph(
        "This report is generated by an AI-assisted compliance tool. Rule thresholds are "
        "representative and must be verified against the current local development authority's "
        "bylaws before submission. This is not a substitute for a licensed architect's or "
        "structural engineer's certification.",
        small
    ))

    doc.build(flow)
