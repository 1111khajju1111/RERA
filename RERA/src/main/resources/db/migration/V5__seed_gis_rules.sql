-- ============================================================
-- V5: Fire access compliance rules (GIS-derived)
--
-- Fire tender access norms vary by state and building height; these are
-- representative NBC figures for demo purposes. Same caveat as V2: cite
-- your actual local fire authority's norms before a real demo.
-- ============================================================

INSERT INTO compliance_rules
    (rule_code, category, description, parameter, operator, threshold_value, unit, applicable_building_type, source_reference)
VALUES
('NBC-FIRE-ACCESS-ROAD-WIDTH', 'FIRE', 'Minimum width of the nearest approach road for fire tender access', 'nearest_road_width_m', '>=', 3.6, 'm', NULL, 'NBC 2016 Part 4, Cl. 4.3 (fire tender access)'),
('NBC-FIRE-ACCESS-MAX-DISTANCE', 'FIRE', 'Maximum distance from the plot to the nearest motorable road', 'nearest_road_distance_m', '<=', 45.0, 'm', NULL, 'NBC 2016 Part 4, Cl. 4.3 (fire tender access)');
