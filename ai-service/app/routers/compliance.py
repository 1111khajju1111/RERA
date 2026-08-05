from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db import get_db
from app.schemas import RunComplianceRequest, RunComplianceResponse
from app.rules_engine.evaluator import evaluate_project, persist_findings
from app.rules_engine.scoring import compute_score
from app.models import ComplianceRule, AuditReport

router = APIRouter(tags=["compliance"])


@router.post("/run-compliance", response_model=RunComplianceResponse)
def run_compliance(request: RunComplianceRequest, db: Session = Depends(get_db)):
    findings = evaluate_project(db, request.project_id)
    persist_findings(db, request.project_id, findings)
    rules_count = db.query(ComplianceRule).count()

    # Snapshot the score right now, while this analysis run's violation set
    # is the current state — see scoring.py docstring for why this replaced
    # computing the same formula again in the Java backend.
    score, approval_probability = compute_score(db, request.project_id)
    db.add(AuditReport(
        project_id=request.project_id,
        project_version_id=request.project_version_id,
        compliance_score=score,
        approval_probability=approval_probability,
        format="SNAPSHOT",  # no file yet — PDF/DOCX/XLSX export is Phase 9
    ))
    db.commit()

    return RunComplianceResponse(
        project_id=request.project_id,
        violations_found=len(findings),
        rules_evaluated=rules_count,
    )
