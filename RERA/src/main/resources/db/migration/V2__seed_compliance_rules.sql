-- ============================================================
-- V2: Seed compliance rules
-- Values below are representative NBC 2016 / typical state UDCPR
-- figures. Before your final demo, replace source_reference with
-- your actual local development authority's bylaw doc + clause —
-- that single detail is what convinces a judge this is real.
-- ============================================================

INSERT INTO compliance_rules
    (rule_code, category, description, parameter, operator, threshold_value, unit, applicable_building_type, source_reference)
VALUES
('NBC-FIRE-EXIT-MIN-WIDTH', 'FIRE', 'Minimum clear width of fire exit/escape route', 'fire_exit_width_m', '>=', 1.5, 'm', NULL, 'NBC 2016 Part 4, Cl. 4.5'),
('NBC-STAIR-MIN-WIDTH', 'NBC', 'Minimum staircase width for residential buildings', 'staircase_width_m', '>=', 1.2, 'm', 'RESIDENTIAL', 'NBC 2016 Part 4, Table 22'),
('RERA-FAR-MAX', 'RERA', 'Maximum permissible Floor Area Ratio', 'far_calculated', '<=', 2.5, 'ratio', NULL, 'State UDCPR, Table 6.1'),
('RERA-GROUND-COVERAGE-MAX', 'RERA', 'Maximum ground coverage of plot area', 'ground_coverage_pct', '<=', 65.0, '%', NULL, 'State UDCPR, Cl. 6.3'),
('NBC-PARKING-RATIO-MIN', 'NBC', 'Minimum parking spaces per dwelling unit', 'parking_ratio', '>=', 1.0, 'per unit', 'RESIDENTIAL', 'State UDCPR, Table 9'),
('NBC-ROOM-MIN-AREA-BEDROOM', 'NBC', 'Minimum habitable room area (bedroom)', 'area_sqm', '>=', 9.5, 'sqm', 'RESIDENTIAL', 'NBC 2016 Part 3, Cl. 3.4'),
('NBC-VENTILATION-MIN-PCT', 'NBC', 'Minimum window-to-floor-area ratio for ventilation', 'window_to_floor_ratio', '>=', 10.0, '%', NULL, 'NBC 2016 Part 3, Cl. 3.5'),
('ACCESS-RAMP-MAX-SLOPE', 'ACCESSIBILITY', 'Maximum ramp slope for wheelchair accessibility', 'ramp_slope_ratio', '<=', 0.083, '1:12', NULL, 'Harmonised Guidelines 2021, Cl. 3.2'),
('NBC-REFUGE-AREA-MIN', 'FIRE', 'Minimum refuge area per 500 sqm of floor above 15m height', 'refuge_area_sqm', '>=', 15.0, 'sqm', NULL, 'NBC 2016 Part 4, Cl. 4.8'),
('NBC-SETBACK-FRONT-MIN', 'NBC', 'Minimum front setback distance', 'front_setback_m', '>=', 3.0, 'm', NULL, 'State UDCPR, Table 5.2'),
('NBC-CORRIDOR-MIN-WIDTH', 'NBC', 'Minimum corridor width for common passage', 'corridor_width_m', '>=', 1.2, 'm', NULL, 'NBC 2016 Part 4, Cl. 4.6');
