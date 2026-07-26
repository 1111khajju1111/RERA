-- ============================================================
-- V3: Synthetic demo data — "Sunrise Residency"
-- A G+3 residential building with THREE deliberate violations
-- and one compliant boundary case, so the demo has something
-- real to detect and explain instead of an empty dashboard.
-- ============================================================

-- 1. Demo user
INSERT INTO users (name, email, password_hash, role)
VALUES ('Demo Architect', 'demo@rera.ai', '$2a$10$placeholderHashChangeMe', 'ARCHITECT');

-- 2. Project
INSERT INTO projects (user_id, name, description, location, plot_area_sqm, status)
SELECT id, 'Sunrise Residency', 'G+3 residential block, synthetic demo dataset', 'Pune, Maharashtra', 500.00, 'AUDITED'
FROM users WHERE email = 'demo@rera.ai';

-- 3. Project version (pretend a DXF was uploaded)
INSERT INTO project_versions (project_id, version_number, file_path, original_filename, file_type, notes)
SELECT id, 1, '/uploads/sunrise_residency_v1.dxf', 'sunrise_residency_v1.dxf', 'DXF', 'Initial synthetic seed for pipeline testing'
FROM projects WHERE name = 'Sunrise Residency';

-- 4. Building
-- ground coverage = 325/500 = 65.0%  -> exactly at threshold (boundary case, compliant)
-- built-up area = 325 + 300*3 = 1225 -> FAR = 1225/500 = 2.45 (compliant, under 2.5)
INSERT INTO buildings (project_id, name, building_type, num_floors, height_m, built_up_area_sqm, far_calculated, ground_coverage_pct)
SELECT id, 'Sunrise Residency Block A', 'RESIDENTIAL', 4, 12.5, 1225.00, 2.450, 65.00
FROM projects WHERE name = 'Sunrise Residency';

-- 5. Floors (0 = ground, 1-3 = upper residential floors)
INSERT INTO floors (building_id, floor_number, floor_height_m, floor_area_sqm)
SELECT id, 0, 3.5, 325.00 FROM buildings WHERE name = 'Sunrise Residency Block A';
INSERT INTO floors (building_id, floor_number, floor_height_m, floor_area_sqm)
SELECT id, 1, 3.0, 300.00 FROM buildings WHERE name = 'Sunrise Residency Block A';
INSERT INTO floors (building_id, floor_number, floor_height_m, floor_area_sqm)
SELECT id, 2, 3.0, 300.00 FROM buildings WHERE name = 'Sunrise Residency Block A';
INSERT INTO floors (building_id, floor_number, floor_height_m, floor_area_sqm)
SELECT id, 3, 3.0, 300.00 FROM buildings WHERE name = 'Sunrise Residency Block A';

-- 6. Rooms on floor 1 — includes ONE undersized bedroom (violation)
INSERT INTO rooms (floor_id, room_type, area_sqm, width_m, length_m, has_natural_light, has_ventilation)
SELECT f.id, 'BEDROOM', 12.0, 3.4, 3.5, true, true
FROM floors f JOIN buildings b ON f.building_id = b.id
WHERE b.name = 'Sunrise Residency Block A' AND f.floor_number = 1;

INSERT INTO rooms (floor_id, room_type, area_sqm, width_m, length_m, has_natural_light, has_ventilation)
SELECT f.id, 'BEDROOM', 8.2, 2.6, 3.15, true, false  -- < 9.5 sqm min AND no ventilation -> 2 violations
FROM floors f JOIN buildings b ON f.building_id = b.id
WHERE b.name = 'Sunrise Residency Block A' AND f.floor_number = 1;

INSERT INTO rooms (floor_id, room_type, area_sqm, width_m, length_m, has_natural_light, has_ventilation)
SELECT f.id, 'KITCHEN', 7.5, 2.5, 3.0, true, true
FROM floors f JOIN buildings b ON f.building_id = b.id
WHERE b.name = 'Sunrise Residency Block A' AND f.floor_number = 1;

INSERT INTO rooms (floor_id, room_type, area_sqm, width_m, length_m, has_natural_light, has_ventilation)
SELECT f.id, 'LIVING', 18.0, 4.0, 4.5, true, true
FROM floors f JOIN buildings b ON f.building_id = b.id
WHERE b.name = 'Sunrise Residency Block A' AND f.floor_number = 1;

-- 7. Components on ground floor — fire exit UNDERSIZED (violation), staircase compliant
INSERT INTO building_components (floor_id, component_type, geometry_json, pos_x, pos_y, width, height, material, confidence_score, detected_by)
SELECT f.id, 'FIRE_EXIT', '{"shape":"door","points":[[0,0],[1.2,0],[1.2,0.2],[0,0.2]]}', 12.0, 0.0, 1.2, 2.1, 'steel', 0.94, 'YOLOv8-fabric-v1'
FROM floors f JOIN buildings b ON f.building_id = b.id
WHERE b.name = 'Sunrise Residency Block A' AND f.floor_number = 0;

INSERT INTO building_components (floor_id, component_type, geometry_json, pos_x, pos_y, width, height, material, confidence_score, detected_by)
SELECT f.id, 'STAIR', '{"shape":"stair","points":[[5,0],[6.35,0],[6.35,4],[5,4]]}', 5.0, 0.0, 1.35, 4.0, 'RCC', 0.97, 'YOLOv8-fabric-v1'
FROM floors f JOIN buildings b ON f.building_id = b.id
WHERE b.name = 'Sunrise Residency Block A' AND f.floor_number = 0;

INSERT INTO building_components (floor_id, component_type, geometry_json, pos_x, pos_y, width, height, material, confidence_score, detected_by)
SELECT f.id, 'WALL', '{"shape":"wall","points":[[0,0],[20,0]]}', 0.0, 0.0, 20.0, 0.23, 'brick', 0.99, 'YOLOv8-fabric-v1'
FROM floors f JOIN buildings b ON f.building_id = b.id
WHERE b.name = 'Sunrise Residency Block A' AND f.floor_number = 0;

-- 8. Fire safety checks
INSERT INTO fire_safety_checks (building_id, check_type, required_value, actual_value, compliant, notes)
SELECT id, 'FIRE_EXIT_WIDTH', 1.5, 1.2, false, 'Detected fire exit width below NBC minimum'
FROM buildings WHERE name = 'Sunrise Residency Block A';

INSERT INTO fire_safety_checks (building_id, check_type, required_value, actual_value, compliant, notes)
SELECT id, 'STAIRCASE_WIDTH', 1.2, 1.35, true, 'Compliant'
FROM buildings WHERE name = 'Sunrise Residency Block A';

-- 9. Parking — UNDER-PROVIDED (violation): 8 units need 8 spaces, only 6 provided
INSERT INTO parking (building_id, required_spaces, provided_spaces, parking_ratio, compliant)
SELECT id, 8, 6, 0.750, false
FROM buildings WHERE name = 'Sunrise Residency Block A';

-- 10. Accessibility — compliant, for variety
INSERT INTO accessibility_checks (building_id, check_type, compliant, notes)
SELECT id, 'ACCESSIBLE_LIFT', true, 'Lift meets minimum car dimensions'
FROM buildings WHERE name = 'Sunrise Residency Block A';

-- 11. Violations — linked to the rules from V2 and the data above
INSERT INTO violations (project_id, rule_id, floor_id, severity, description, detected_value, required_value, status)
SELECT p.id, r.id, f.id, 'CRITICAL',
       'Fire exit width on ground floor is below the NBC minimum, restricting evacuation capacity.',
       1.2, 1.5, 'OPEN'
FROM projects p, compliance_rules r, floors f
JOIN buildings b ON f.building_id = b.id
WHERE p.name = 'Sunrise Residency' AND r.rule_code = 'NBC-FIRE-EXIT-MIN-WIDTH'
  AND b.name = 'Sunrise Residency Block A' AND f.floor_number = 0;

INSERT INTO violations (project_id, rule_id, floor_id, severity, description, detected_value, required_value, status)
SELECT p.id, r.id, f.id, 'MAJOR',
       'A bedroom on floor 1 is undersized relative to the minimum habitable room area.',
       8.2, 9.5, 'OPEN'
FROM projects p, compliance_rules r, floors f
JOIN buildings b ON f.building_id = b.id
WHERE p.name = 'Sunrise Residency' AND r.rule_code = 'NBC-ROOM-MIN-AREA-BEDROOM'
  AND b.name = 'Sunrise Residency Block A' AND f.floor_number = 1;

INSERT INTO violations (project_id, rule_id, floor_id, severity, description, detected_value, required_value, status)
SELECT p.id, r.id, NULL, 'MAJOR',
       'Provided parking spaces fall short of the required ratio for the number of dwelling units.',
       0.750, 1.0, 'OPEN'
FROM projects p, compliance_rules r
WHERE p.name = 'Sunrise Residency' AND r.rule_code = 'NBC-PARKING-RATIO-MIN';
