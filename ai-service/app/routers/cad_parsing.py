from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.db import get_db
from app.schemas import ParseCadRequest, ParseCadResponse
from app.cad_parsing.dxf_parser import parse_dxf
from app.cad_parsing.ingest import ingest_parsed_dxf

router = APIRouter(tags=["cad-parsing"])


@router.post("/parse-cad", response_model=ParseCadResponse)
def parse_cad(request: ParseCadRequest, db: Session = Depends(get_db)):
    file_path = request.file_path
    if file_path.lower().endswith(".dxf"):
        parsed = parse_dxf(file_path)
    elif file_path.lower().endswith(".ifc"):
        raise HTTPException(status_code=501, detail="IFC parsing not yet implemented — see ifc_parser.py")
    elif file_path.lower().endswith(".dwg"):
        raise HTTPException(
            status_code=501,
            detail="DWG parsing not supported — convert to DXF first (e.g. via ODA File Converter)",
        )
    else:
        raise HTTPException(status_code=400, detail=f"Unsupported file type: {file_path}")

    counts = ingest_parsed_dxf(db, request.project_id, parsed)

    return ParseCadResponse(
        project_id=request.project_id,
        floors_created=counts["floors_created"],
        rooms_created=counts["rooms_created"],
        components_created=counts["components_created"],
        warnings=parsed["warnings"],
    )
