-- ============================================================
-- V8: Project Timeline / Version History (Phase A)
--
-- Links each compliance score snapshot to the specific upload that
-- produced it, so "version history" can show how the score actually
-- changed as the architect revised and re-uploaded their drawing.
-- ============================================================

ALTER TABLE audit_reports ADD COLUMN project_version_id BIGINT REFERENCES project_versions(id) ON DELETE SET NULL;
CREATE INDEX idx_audit_reports_version ON audit_reports(project_version_id);
