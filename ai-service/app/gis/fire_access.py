"""
Evaluates the two GIS-derived compliance rules (V5 seed migration:
NBC-FIRE-ACCESS-ROAD-WIDTH, NBC-FIRE-ACCESS-MAX-DISTANCE) against a
project's SiteAnalysis row, using the same operator-dispatch pattern as
app/rules_engine/evaluator.py — kept separate from that module since
these are site-level checks (no floor/component), not building-geometry
checks, but they still write to the same `violations` table so they show
up in the same dashboard the DXF-derived violations do.
"""

from decimal import Decimal
from sqlalchemy.orm import Session

from app.models import ComplianceRule, Violation, SiteAnalysis
from app.rules_engine.evaluator import OPERATORS, severity_for_rule

GIS_RULE_CODES = ["NBC-FIRE-ACCESS-ROAD-WIDTH", "NBC-FIRE-ACCESS-MAX-DISTANCE"]


def evaluate_and_persist_fire_access(db: Session, project_id: int, site: SiteAnalysis) -> bool:
    """Returns overall fire_access_compliant boolean; also writes/clears violations."""
    rules = db.query(ComplianceRule).filter(ComplianceRule.rule_code.in_(GIS_RULE_CODES)).all()

    # Idempotent re-run, scoped only to these two rule IDs so it doesn't
    # touch DXF-derived violations from the main rule engine.
    rule_ids = [r.id for r in rules]
    if rule_ids:
        db.query(Violation).filter(
            Violation.project_id == project_id,
            Violation.rule_id.in_(rule_ids),
            Violation.status == "OPEN",
        ).delete(synchronize_session=False)

    value_map = {
        "nearest_road_width_m": site.nearest_road_width_m,
        "nearest_road_distance_m": site.nearest_road_distance_m,
    }

    all_compliant = True
    for rule in rules:
        measured = value_map.get(rule.parameter)
        if measured is None:
            continue  # no road found nearby — nothing to evaluate yet

        op = OPERATORS.get(rule.operator)
        passes = op(measured, rule.threshold_value) if op else True
        if not passes:
            all_compliant = False
            db.add(Violation(
                project_id=project_id,
                rule_id=rule.id,
                floor_id=None,
                component_id=None,
                severity=severity_for_rule(rule),
                description=(
                    f"{rule.description} — measured {measured}{rule.unit or ''}, "
                    f"required {rule.operator} {rule.threshold_value}{rule.unit or ''}. "
                    f"{'(Width is an estimate based on road classification, not a direct measurement.)' if rule.parameter == 'nearest_road_width_m' and site.nearest_road_width_is_estimated else ''}"
                ).strip(),
                detected_value=measured,
                required_value=rule.threshold_value,
                status="OPEN",
            ))

    db.commit()
    return all_compliant
