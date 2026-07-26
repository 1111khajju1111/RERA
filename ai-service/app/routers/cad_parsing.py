from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

import base64
import os
import tempfile

from app.db import get_db
from app.schemas import ParseCadRequest, ParseCadResponse
from app.cad_parsing.dxf_parser import parse_dxf
from app.cad_parsing.ingest import ingest_parsed_dxf

router = APIRouter(tags=["cad-parsing"])


@router.post("/parse-cad", response_model=ParseCadResponse)
def parse_cad(request: ParseCadRequest, db: Session = Depends(get_db)):
    file_path = request.file_path
    suffix = os.path.splitext(file_path)[1].lower()

    if suffix not in (".dxf", ".ifc", ".dwg"):
        raise HTTPException(status_code=400, detail=f"Unsupported file type: {file_path}")
    if suffix == ".ifc":
        raise HTTPException(status_code=501, detail="IFC parsing not yet implemented — see ifc_parser.py")
    if suffix == ".dwg":
        raise HTTPException(
            status_code=501,
            detail="DWG parsing not supported — convert to DXF first (e.g. via ODA File Converter)",
        )

    if request.file_content_base64 is not None:
        # Cross-service deployment: write the decoded bytes to a local temp
        # file so ezdxf has an actual path to open, then clean up.
        try:
            raw = base64.b64decode(request.file_content_base64)
        except Exception:
            raise HTTPException(status_code=400, detail="file_content_base64 is not valid base64")
        with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
            tmp.write(raw)
            tmp_path = tmp.name
        try:
            parsed = parse_dxf(tmp_path)
        finally:
            os.unlink(tmp_path)
    else:
        # Local dev fallback: backend and ai-service on the same machine,
        # sharing the same filesystem (see ai-service/README.md step 2).
        if not os.path.exists(file_path):
            raise HTTPException(
                status_code=400,
                detail=f"file_path '{file_path}' is not readable from this service, and no "
                       f"file_content_base64 was provided.",
            )
        parsed = parse_dxf(file_path)

    counts = ingest_parsed_dxf(db, request.project_id, parsed)

    return ParseCadResponse(
        project_id=request.project_id,
        floors_created=counts["floors_created"],
        rooms_created=counts["rooms_created"],
        components_created=counts["components_created"],
        warnings=parsed["warnings"],
    )
