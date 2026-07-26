// Mirrors the DTO records in backend/src/main/java/com/rera/auditor/dto/
// Kept as a single source of truth so a backend field rename gets caught
// by TypeScript instead of failing silently at runtime.

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
}

export interface ProjectResponse {
  id: number;
  name: string;
  description: string | null;
  location: string | null;
  plotAreaSqm: number | null;
  status: "DRAFT" | "PROCESSING" | "AUDITED" | "APPROVED" | "REJECTED" | "PROCESSING_FAILED";
  createdAt: string;
  updatedAt: string;
}

export interface RoomResponse {
  id: number;
  roomType: string;
  areaSqm: number | null;
  widthM: number | null;
  lengthM: number | null;
  hasNaturalLight: boolean;
  hasVentilation: boolean;
}

export interface ComponentResponse {
  id: number;
  componentType: string;
  geometryJson: string;
  posX: number | null;
  posY: number | null;
  width: number | null;
  height: number | null;
  material: string | null;
  confidenceScore: number | null;
  detectedBy: string | null;
}

export interface FloorResponse {
  id: number;
  floorNumber: number;
  floorHeightM: number | null;
  floorAreaSqm: number | null;
  rooms: RoomResponse[];
  components: ComponentResponse[];
}

export interface BuildingResponse {
  id: number;
  name: string;
  buildingType: string;
  numFloors: number;
  heightM: number | null;
  builtUpAreaSqm: number | null;
  farCalculated: number | null;
  groundCoveragePct: number | null;
  floors: FloorResponse[];
}

export type Severity = "CRITICAL" | "MAJOR" | "MINOR";

export interface ViolationResponse {
  id: number;
  ruleCode: string;
  category: string;
  severity: Severity;
  description: string;
  detectedValue: number | null;
  requiredValue: number | null;
  unit: string | null;
  status: string;
  floorNumber: number | null;
  componentType: string | null;
  componentId: number | null;
  resolutionNote: string | null;
  detectedAt: string;
}

export interface ComplianceHistoryEntry {
  complianceScore: number | null;
  approvalProbability: number | null;
  generatedAt: string;
}

export interface ComplianceSummaryResponse {
  complianceScore: number | null;
  approvalProbability: number | null;
  totalViolations: number;
  criticalViolations: number;
  majorViolations: number;
  minorViolations: number;
}

export interface ChatMessageResponse {
  id: number;
  role: "USER" | "ASSISTANT";
  message: string;
  createdAt: string;
}

export interface SiteAnalysisResponse {
  latitude: number | null;
  longitude: number | null;
  geocodedAddress: string | null;
  nearestRoadDistanceM: number | null;
  nearestRoadWidthM: number | null;
  nearestRoadWidthIsEstimated: boolean | null;
  nearestRoadType: string | null;
  nearestRoadName: string | null;
  fireAccessCompliant: boolean | null;
  encroachmentStatus: string;
  encroachmentNotes: string | null;
  nearbyRoadsGeojson: string | null; // JSON-stringified GeoJSON FeatureCollection
  analyzedAt: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details: string[] | null;
}
