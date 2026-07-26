-- ============================================================
-- V6: Compliance engine refinement (Phase 8)
--
-- 1. Rule-specific severity, overriding the previous category-based
--    inference (all FIRE -> CRITICAL, all RERA/NBC -> MAJOR, else MINOR).
--    That blanket mapping meant e.g. a minor ventilation shortfall and a
--    structurally undersized fire exit were both automatically CRITICAL
--    just for sharing the FIRE category — too coarse for a real severity
--    signal. Backfilled below with the same values the old logic implied,
--    so nothing changes until you deliberately override a specific rule.
-- ============================================================

ALTER TABLE compliance_rules ADD COLUMN default_severity VARCHAR(20);

UPDATE compliance_rules SET default_severity = 'CRITICAL' WHERE category = 'FIRE';
UPDATE compliance_rules SET default_severity = 'MAJOR' WHERE category IN ('RERA', 'NBC') AND default_severity IS NULL;
UPDATE compliance_rules SET default_severity = 'MINOR' WHERE default_severity IS NULL;

ALTER TABLE compliance_rules ALTER COLUMN default_severity SET NOT NULL;

-- Example of the kind of per-rule override this now enables — a bedroom
-- slightly under the minimum area is a real but lesser issue than a
-- fire safety failure, even though the earlier logic bucketed both rules
-- from NBC as MAJOR by category alone. Left as MAJOR here since that's
-- still a defensible call; shown as a worked example for future tuning.
-- UPDATE compliance_rules SET default_severity = 'MINOR' WHERE rule_code = 'NBC-ROOM-MIN-AREA-BEDROOM';

-- 2. Violation lifecycle support. The `status` column already existed
--    (OPEN/RESOLVED/WAIVED) but nothing ever wrote RESOLVED or WAIVED —
--    there was no endpoint to do it. Adding a note column so an architect
--    can record *why* something was waived (e.g. "local authority granted
--    a variance") rather than the status change being unexplained.
ALTER TABLE violations ADD COLUMN resolution_note TEXT;
