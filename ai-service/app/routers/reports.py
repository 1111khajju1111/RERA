import os
from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import FileResponse
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

CONTENT_TYPES = {
    "PDF": "application/pdf",
    "DOCX": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "XLSX": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
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


@router.get("/reports/{report_id}/file")
def download_report_file(report_id: int, db: Session = Depends(get_db)):
    """
    Streams the actual bytes of a previously generated report.

    The backend and this service are two separate Render services with
    separate filesystems, so the backend cannot read `file_path` directly
    off its own disk. It instead proxies through here: fetch bytes from
    this endpoint and stream them back to the browser.
    """
    report = db.query(AuditReport).filter(AuditReport.id == report_id).first()
    if report is None:
        raise HTTPException(status_code=404, detail=f"Report not found: {report_id}")
    if not report.file_path:
        raise HTTPException(status_code=404, detail="This report has no downloadable file")
    if not os.path.exists(report.file_path):
        raise HTTPException(
            status_code=404,
            detail=f"Report file is missing on disk at {report.file_path} — it may have been "
                   f"generated before a deploy that cleared this service's ephemeral disk, or on "
                   f"a different ai-service instance. Regenerate the report.",
        )

    return FileResponse(
        path=report.file_path,
        media_type=CONTENT_TYPES.get(report.format, "application/octet-stream"),
        filename=os.path.basename(report.file_path),
    )
