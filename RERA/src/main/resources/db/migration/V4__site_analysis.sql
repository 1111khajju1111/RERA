-- ============================================================
-- V4: Site analysis (GIS) — Phase 7
--
-- One row per project. Plot boundary here is a POINT (geocoded or
-- user-placed on the map), not a survey polygon — see AI service
-- Phase 7 README for why a true boundary polygon isn't auto-sourced.
-- ============================================================

CREATE TABLE site_analysis (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL UNIQUE REFERENCES projects(id) ON DELETE CASCADE,

    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    geocoded_address TEXT,

    nearest_road_distance_m DECIMAL(8, 2),
    nearest_road_width_m DECIMAL(6, 2),
    nearest_road_width_is_estimated BOOLEAN DEFAULT true, -- true unless OSM's `width` tag was actually present
    nearest_road_type VARCHAR(50),   -- OSM highway=* tag value (e.g. 'residential', 'primary')
    nearest_road_name VARCHAR(255),

    fire_access_compliant BOOLEAN,

    encroachment_status VARCHAR(30) NOT NULL DEFAULT 'NOT_AVAILABLE', -- see gis/encroachment.py — always NOT_AVAILABLE for now
    encroachment_notes TEXT,

    nearby_roads_geojson JSONB,     -- for the Leaflet map: the actual OSM ways fetched
    analyzed_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_site_analysis_project ON site_analysis(project_id);
