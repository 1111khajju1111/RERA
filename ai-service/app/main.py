"""
FastAPI AI microservice entrypoint.

Note: this service does NOT call Base.metadata.create_all() — the schema
is owned by the Spring Boot backend's Flyway migrations (Phase 2/3). This
service only reads/writes tables that already exist. Running this against
a database that hasn't had the Flyway migrations applied will fail loudly
on the first query, which is the correct behavior (better than silently
creating a divergent schema).
"""

from fastapi import FastAPI

from app.routers import cad_parsing, compliance, suggestions, chat, gis, reports

app = FastAPI(
    title="AI RERA Auditor — AI Service",
    description="CAD parsing, rule evaluation, and LLM explanation service",
    version="0.1.0",
)

app.include_router(cad_parsing.router)
app.include_router(compliance.router)
app.include_router(suggestions.router)
app.include_router(chat.router)
app.include_router(gis.router)
app.include_router(reports.router)


@app.get("/health")
def health():
    return {"status": "ok"}
