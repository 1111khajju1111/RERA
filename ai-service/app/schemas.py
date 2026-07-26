from pydantic import BaseModel
from typing import Optional


class ParseCadRequest(BaseModel):
    project_id: int
    file_path: str
    # Base64-encoded raw file bytes. The backend and this service are
    # deployed as separate Render services with separate filesystems, so
    # file_path alone (a path on the *backend's* disk) is not readable
    # here. When present, this is parsed instead of touching file_path
    # directly; file_path is kept only to recover the original extension
    # and for local dev use (see ai-service/README.md), where both
    # services may share a filesystem.
    file_content_base64: Optional[str] = None


class ParseCadResponse(BaseModel):
    project_id: int
    floors_created: int
    rooms_created: int
    components_created: int
    warnings: list[str] = []


class RunComplianceRequest(BaseModel):
    project_id: int


class RunComplianceResponse(BaseModel):
    project_id: int
    violations_found: int
    rules_evaluated: int


class GenerateSuggestionsRequest(BaseModel):
    project_id: int


class GenerateSuggestionsResponse(BaseModel):
    project_id: int
    suggestions_created: int
    llm_used: bool


class ChatRequest(BaseModel):
    project_id: int
    message: str


class ChatResponse(BaseModel):
    reply: str
    llm_used: bool


class GisAnalyzeRequest(BaseModel):
    project_id: int
    address: str


class GisAnalyzeResponse(BaseModel):
    project_id: int
    latitude: Optional[float]
    longitude: Optional[float]
    geocoded_address: Optional[str]
    nearest_road_distance_m: Optional[float]
    nearest_road_width_m: Optional[float]
    fire_access_compliant: Optional[bool]
    encroachment_status: str
    warnings: list[str] = []


class GenerateReportRequest(BaseModel):
    project_id: int
    format: str  # PDF, DOCX, XLSX


class GenerateReportResponse(BaseModel):
    project_id: int
    format: str
    file_path: str
    compliance_score: Optional[float]
    approval_probability: Optional[float]
