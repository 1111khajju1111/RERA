from pydantic import BaseModel
from typing import Optional


class ParseCadRequest(BaseModel):
    project_id: int
    file_path: str


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
