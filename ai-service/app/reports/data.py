"""
Gathers everything a report needs into one plain dict, so pdf_report.py,
docx_report.py, and xlsx_report.py all render from the exact same data —
no risk of the three formats silently disagreeing on numbers because one
of them queried the DB slightly differently.
"""

from sqlalchemy.orm import Session

from app.models import (
    Project, Building, Floor, Violation, AiSuggestion, SiteAnalysis, AuditReport
)


def gather_report_data(db: Session, project_id: int) -> dict:
    project = db.query(Project).filter(Project.id == project_id).first()
    if project is None:
        raise ValueError(f"Project {project_id} not found")

    buildings = db.query(Building).filter(Building.project_id == project_id).all()
    violations = db.query(Violation).filter(
        Violation.project_id == project_id, Violation.status == "OPEN"
    ).order_by(Violation.severity).all()
    suggestions = db.query(AiSuggestion).filter(AiSuggestion.project_id == project_id).all()
    site = db.query(SiteAnalysis).filter(SiteAnalysis.project_id == project_id).first()
    latest_report = db.query(AuditReport).filter(
        AuditReport.project_id == project_id
    ).order_by(AuditReport.generated_at.desc()).first()

    violations_by_severity = {"CRITICAL": [], "MAJOR": [], "MINOR": []}
    for v in violations:
        violations_by_severity.setdefault(v.severity, []).append(v)

    return {
        "project": project,
        "buildings": buildings,
        "violations": violations,
        "violations_by_severity": violations_by_severity,
        "suggestions": suggestions,
        "site": site,
        "compliance_score": latest_report.compliance_score if latest_report else None,
        "approval_probability": latest_report.approval_probability if latest_report else None,
    }
