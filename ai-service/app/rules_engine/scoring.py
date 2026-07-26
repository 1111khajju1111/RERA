"""
Single source of truth for the compliance score / approval probability
formula. Previously this same heuristic was duplicated in the Java
backend's ComplianceService, computed live from violation counts on every
GET request. That meant two implementations of one formula that could
silently drift apart if either side was tweaked independently.

Now: the AI service computes and freezes a score into `audit_reports`
immediately after each rule-engine run (see routers/compliance.py), and
the backend just reads the latest snapshot. This module is the only place
the formula lives.

Still a simple, explainable heuristic — not a learned/calibrated model.
Tune the constants once you have real approval/rejection outcomes to
check them against.
"""

from decimal import Decimal
from sqlalchemy.orm import Session

from app.models import Violation

CRITICAL_PENALTY = Decimal("15")
MAJOR_PENALTY = Decimal("8")
MINOR_PENALTY = Decimal("3")


def compute_score(db: Session, project_id: int) -> tuple[Decimal, Decimal]:
    """Returns (compliance_score, approval_probability), both 0-100."""
    open_violations = db.query(Violation).filter(
        Violation.project_id == project_id, Violation.status == "OPEN"
    ).all()

    critical = sum(1 for v in open_violations if v.severity == "CRITICAL")
    major = sum(1 for v in open_violations if v.severity == "MAJOR")
    minor = sum(1 for v in open_violations if v.severity == "MINOR")

    penalty = CRITICAL_PENALTY * critical + MAJOR_PENALTY * major + MINOR_PENALTY * minor
    score = max(Decimal("0"), Decimal("100") - penalty)

    # Approval probability skews harsher when any CRITICAL violation is
    # open, since those categories (fire safety) are typically hard
    # rejections rather than negotiable deductions.
    approval_probability = score * Decimal("0.5") if critical > 0 else score

    return round(score, 2), round(approval_probability, 2)
