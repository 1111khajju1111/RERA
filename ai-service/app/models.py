"""
SQLAlchemy models mirroring the schema owned by the Spring Boot backend
(see backend/src/main/resources/db/migration/V1__init_schema.sql).

The AI service reads/writes these same tables directly rather than going
through the backend's REST API. This is a deliberate simplification for
the hackathon build: it avoids building and maintaining a parallel
callback-ingestion API on the Java side. The trade-off is that both
services now need to agree on schema changes — acceptable for a single-repo
hackathon project, worth revisiting (event-driven or callback-based) if
this ever needs two independently-deployed teams.
"""

from sqlalchemy import (
    Column, BigInteger, Integer, String, Text, Numeric, Boolean,
    DateTime, ForeignKey, JSON
)
from sqlalchemy.orm import relationship
from datetime import datetime

from app.db import Base


class Project(Base):
    __tablename__ = "projects"
    id = Column(BigInteger, primary_key=True)
    user_id = Column(BigInteger, ForeignKey("users.id"))
    name = Column(String(200))
    description = Column(Text)
    location = Column(String(255))
    plot_area_sqm = Column(Numeric(10, 2))
    status = Column(String(30))
    created_at = Column(DateTime)
    updated_at = Column(DateTime)

    buildings = relationship("Building", back_populates="project")


class Building(Base):
    __tablename__ = "buildings"
    id = Column(BigInteger, primary_key=True)
    project_id = Column(BigInteger, ForeignKey("projects.id"))
    name = Column(String(150))
    building_type = Column(String(50))
    num_floors = Column(Integer)
    height_m = Column(Numeric(6, 2))
    built_up_area_sqm = Column(Numeric(10, 2))
    far_calculated = Column(Numeric(6, 3))
    ground_coverage_pct = Column(Numeric(5, 2))

    project = relationship("Project", back_populates="buildings")
    floors = relationship("Floor", back_populates="building")


class Floor(Base):
    __tablename__ = "floors"
    id = Column(BigInteger, primary_key=True)
    building_id = Column(BigInteger, ForeignKey("buildings.id"))
    floor_number = Column(Integer)
    floor_height_m = Column(Numeric(5, 2))
    floor_area_sqm = Column(Numeric(10, 2))

    building = relationship("Building", back_populates="floors")
    rooms = relationship("Room", back_populates="floor")
    components = relationship("BuildingComponent", back_populates="floor")


class Room(Base):
    __tablename__ = "rooms"
    id = Column(BigInteger, primary_key=True)
    floor_id = Column(BigInteger, ForeignKey("floors.id"))
    room_type = Column(String(50))
    area_sqm = Column(Numeric(8, 2))
    width_m = Column(Numeric(6, 2))
    length_m = Column(Numeric(6, 2))
    has_natural_light = Column(Boolean, default=False)
    has_ventilation = Column(Boolean, default=False)

    floor = relationship("Floor", back_populates="rooms")


class BuildingComponent(Base):
    __tablename__ = "building_components"
    id = Column(BigInteger, primary_key=True)
    floor_id = Column(BigInteger, ForeignKey("floors.id"))
    component_type = Column(String(30))
    geometry_json = Column(JSON)
    pos_x = Column(Numeric(10, 3))
    pos_y = Column(Numeric(10, 3))
    width = Column(Numeric(8, 3))
    height = Column(Numeric(8, 3))
    material = Column(String(50))
    confidence_score = Column(Numeric(4, 3))
    detected_by = Column(String(50))

    floor = relationship("Floor", back_populates="components")


class ComplianceRule(Base):
    __tablename__ = "compliance_rules"
    id = Column(BigInteger, primary_key=True)
    rule_code = Column(String(30))
    category = Column(String(30))
    description = Column(Text)
    parameter = Column(String(50))
    operator = Column(String(10))
    threshold_value = Column(Numeric(10, 3))
    unit = Column(String(20))
    applicable_building_type = Column(String(50))
    source_reference = Column(String(255))
    default_severity = Column(String(20))


class Violation(Base):
    __tablename__ = "violations"
    id = Column(BigInteger, primary_key=True)
    project_id = Column(BigInteger, ForeignKey("projects.id"))
    rule_id = Column(BigInteger, ForeignKey("compliance_rules.id"))
    component_id = Column(BigInteger, ForeignKey("building_components.id"), nullable=True)
    floor_id = Column(BigInteger, ForeignKey("floors.id"), nullable=True)
    severity = Column(String(20))
    description = Column(Text)
    detected_value = Column(Numeric(10, 3))
    required_value = Column(Numeric(10, 3))
    status = Column(String(20), default="OPEN")
    resolution_note = Column(Text)
    detected_at = Column(DateTime, default=datetime.utcnow)

    rule = relationship("ComplianceRule")


class AiSuggestion(Base):
    __tablename__ = "ai_suggestions"
    id = Column(BigInteger, primary_key=True)
    project_id = Column(BigInteger, ForeignKey("projects.id"))
    violation_id = Column(BigInteger, ForeignKey("violations.id"), nullable=True)
    suggestion_text = Column(Text)
    category = Column(String(30))
    created_at = Column(DateTime, default=datetime.utcnow)


class ChatMessage(Base):
    __tablename__ = "chat_history"
    id = Column(BigInteger, primary_key=True)
    project_id = Column(BigInteger, ForeignKey("projects.id"))
    user_id = Column(BigInteger, nullable=True)
    role = Column(String(10))
    message = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)


class AuditReport(Base):
    __tablename__ = "audit_reports"
    id = Column(BigInteger, primary_key=True)
    project_id = Column(BigInteger, ForeignKey("projects.id"))
    # No ForeignKey(...) here on purpose: the DB-level constraint already
    # exists (added in V8__version_score_linkage.sql, referencing
    # project_versions.id). Declaring it here too would require mapping a
    # ProjectVersion class in this file so SQLAlchemy can resolve the
    # target table — which nothing here actually needs, since ai-service
    # only ever writes/reads this as a plain id, never traverses a
    # relationship to it. Without a mapped target class, SQLAlchemy can't
    # resolve ForeignKey("project_versions.id") and raises
    # NoReferencedTableError at mapper-configuration time, breaking every
    # query through this model — not just ones touching this column.
    project_version_id = Column(BigInteger, nullable=True)
    compliance_score = Column(Numeric(5, 2))
    approval_probability = Column(Numeric(5, 2))
    file_path = Column(String(500))
    format = Column(String(10))
    generated_at = Column(DateTime, default=datetime.utcnow)


class SiteAnalysis(Base):
    __tablename__ = "site_analysis"
    id = Column(BigInteger, primary_key=True)
    project_id = Column(BigInteger, ForeignKey("projects.id"), unique=True)
    latitude = Column(Numeric(10, 7))
    longitude = Column(Numeric(10, 7))
    geocoded_address = Column(Text)
    nearest_road_distance_m = Column(Numeric(8, 2))
    nearest_road_width_m = Column(Numeric(6, 2))
    nearest_road_width_is_estimated = Column(Boolean, default=True)
    nearest_road_type = Column(String(50))
    nearest_road_name = Column(String(255))
    fire_access_compliant = Column(Boolean)
    encroachment_status = Column(String(30), default="NOT_AVAILABLE")
    encroachment_notes = Column(Text)
    nearby_roads_geojson = Column(JSON)
    analyzed_at = Column(DateTime, default=datetime.utcnow)
