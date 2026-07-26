export interface ComponentTypeStyle {
  verticalHeight: number;
  yStart: number;
  color: string;
  opacity: number;
}

export const TOGGLEABLE_COMPONENT_TYPES = [
  "WALL", "DOOR", "WINDOW", "COLUMN", "STAIR", "FIRE_EXIT", "CORRIDOR",
] as const;

export type ComponentType = (typeof TOGGLEABLE_COMPONENT_TYPES)[number];

// Not modeled in the current schema (see backend V1__init_schema.sql —
// building_components.component_type has no PLUMBING/ELECTRICAL values,
// and the DXF parser's layer_mapping.py doesn't classify them either).
// Listed here so the toolbar can show them as disabled with an honest
// "not yet detected" state instead of omitting them and leaving a silent
// gap against the original feature list.
export const UNMODELED_COMPONENT_TYPES = ["PLUMBING", "ELECTRICAL"] as const;
