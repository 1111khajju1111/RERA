-- ============================================================
-- AI RERA Auditor — V1: Initial Schema
-- Run via Flyway (backend/src/main/resources/db/migration/)
-- ============================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ARCHITECT',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    plot_area_sqm DECIMAL(10,2),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_projects_user ON projects(user_id);

CREATE TABLE project_versions (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    notes TEXT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(project_id, version_number)
);

CREATE TABLE buildings (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    building_type VARCHAR(50) NOT NULL,
    num_floors INT NOT NULL,
    height_m DECIMAL(6,2),
    built_up_area_sqm DECIMAL(10,2),
    far_calculated DECIMAL(6,3),
    ground_coverage_pct DECIMAL(5,2)
);

CREATE TABLE floors (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    floor_number INT NOT NULL,
    floor_height_m DECIMAL(5,2),
    floor_area_sqm DECIMAL(10,2),
    UNIQUE(building_id, floor_number)
);

CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    floor_id BIGINT NOT NULL REFERENCES floors(id) ON DELETE CASCADE,
    room_type VARCHAR(50) NOT NULL,
    area_sqm DECIMAL(8,2),
    width_m DECIMAL(6,2),
    length_m DECIMAL(6,2),
    has_natural_light BOOLEAN DEFAULT false,
    has_ventilation BOOLEAN DEFAULT false
);

CREATE TABLE building_components (
    id BIGSERIAL PRIMARY KEY,
    floor_id BIGINT NOT NULL REFERENCES floors(id) ON DELETE CASCADE,
    component_type VARCHAR(30) NOT NULL,
    geometry_json JSONB NOT NULL,
    pos_x DECIMAL(10,3),
    pos_y DECIMAL(10,3),
    width DECIMAL(8,3),
    height DECIMAL(8,3),
    material VARCHAR(50),
    confidence_score DECIMAL(4,3),
    detected_by VARCHAR(50) -- e.g. 'YOLOv8-fabric-v1', 'manual', 'sam2'
);
CREATE INDEX idx_components_floor ON building_components(floor_id);
CREATE INDEX idx_components_type ON building_components(component_type);

CREATE TABLE compliance_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(30) UNIQUE NOT NULL,
    category VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    parameter VARCHAR(50) NOT NULL,
    operator VARCHAR(10) NOT NULL,
    threshold_value DECIMAL(10,3) NOT NULL,
    unit VARCHAR(20),
    applicable_building_type VARCHAR(50),
    source_reference VARCHAR(255) -- e.g. 'NBC 2016 Part 4, Table 22' — cite it for credibility
);

CREATE TABLE violations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    rule_id BIGINT NOT NULL REFERENCES compliance_rules(id),
    component_id BIGINT REFERENCES building_components(id) ON DELETE SET NULL,
    floor_id BIGINT REFERENCES floors(id) ON DELETE SET NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    detected_value DECIMAL(10,3),
    required_value DECIMAL(10,3),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    detected_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_violations_project ON violations(project_id);

CREATE TABLE fire_safety_checks (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    check_type VARCHAR(50) NOT NULL,
    required_value DECIMAL(10,3),
    actual_value DECIMAL(10,3),
    compliant BOOLEAN NOT NULL,
    notes TEXT
);

CREATE TABLE parking (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    required_spaces INT NOT NULL,
    provided_spaces INT NOT NULL,
    parking_ratio DECIMAL(6,3),
    compliant BOOLEAN NOT NULL
);

CREATE TABLE accessibility_checks (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    check_type VARCHAR(50) NOT NULL,
    compliant BOOLEAN NOT NULL,
    notes TEXT
);

CREATE TABLE audit_reports (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    compliance_score DECIMAL(5,2),
    approval_probability DECIMAL(5,2),
    file_path VARCHAR(500),
    format VARCHAR(10),
    generated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ai_suggestions (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    violation_id BIGINT REFERENCES violations(id) ON DELETE SET NULL,
    suggestion_text TEXT NOT NULL,
    category VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE chat_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    role VARCHAR(10) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_chat_project ON chat_history(project_id);
