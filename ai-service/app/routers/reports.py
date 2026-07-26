import os
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.db import get_db
from app.config import settings
from app.schemas import GenerateReportRequest, GenerateReportResponse
from app.reports.data import gather_report_data
from app.reports.pdf_report import generate_pdf
from app.reports.docx_report import generate_docx
from app.reports.xlsx_report import generate_xlsx
from app.models import AuditReport

router = APIRouter(tags=["reports"])

GENERATORS = {
    "PDF": (generate_pdf, "pdf"),
    "DOCX": (generate_docx, "docx"),
    "XLSX": (generate_xlsx, "xlsx"),
}


@router.post("/reports/generate", response_model=GenerateReportResponse)
def generate_report(request: GenerateReportRequest, db: Session = Depends(get_db)):
    fmt = request.format.upper()
    if fmt not in GENERATORS:
        raise HTTPException(status_code=400, detail=f"Unsupported format: {request.format}. Use PDF, DOCX, or XLSX.")

    try:
        data = gather_report_data(db, request.project_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))

    generator_fn, extension = GENERATORS[fmt]

    reports_dir = os.path.join(settings.reports_dir, str(request.project_id))
    os.makedirs(reports_dir, exist_ok=True)
    filename = f"report_{request.project_id}.{extension}"
    output_path = os.path.join(reports_dir, filename)

    generator_fn(data, output_path)

    audit_report = AuditReport(
        project_id=request.project_id,
        compliance_score=data["compliance_score"],
        approval_probability=data["approval_probability"],
        file_path=output_path,
        format=fmt,
    )
    db.add(audit_report)
    db.commit()

    return GenerateReportResponse(
        project_id=request.project_id,
        format=fmt,
        file_path=output_path,
        compliance_score=float(data["compliance_score"]) if data["compliance_score"] is not None else None,
        approval_probability=float(data["approval_probability"]) if data["approval_probability"] is not None else None,
    )
