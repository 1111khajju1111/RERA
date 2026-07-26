"""
Project-grounded chat. Every reply is built from the project's actual
violations/suggestions in the DB — the LLM (when available) is used to
phrase the answer naturally, not to invent facts about the building.
Without an API key, it still returns a real, useful answer assembled
directly from the data, just with less natural phrasing.

Provider: Groq, using openai/gpt-oss-120b by default (see config.py).
"""

from sqlalchemy.orm import Session

from app.config import settings
from app.models import Violation, AiSuggestion

_client = None
if settings.groq_api_key:
    from groq import Groq
    _client = Groq(api_key=settings.groq_api_key)


def _build_context(db: Session, project_id: int) -> str:
    violations = db.query(Violation).filter(
        Violation.project_id == project_id, Violation.status == "OPEN"
    ).all()
    suggestions = db.query(AiSuggestion).filter(AiSuggestion.project_id == project_id).all()

    if not violations:
        return "This project currently has no open violations on record."

    lines = [f"This project has {len(violations)} open violation(s):"]
    for v in violations:
        lines.append(f"- [{v.severity}] {v.description}")
    if suggestions:
        lines.append("\nExisting AI suggestions on file:")
        for s in suggestions[:5]:
            lines.append(f"- {s.suggestion_text}")
    return "\n".join(lines)


def _fallback_reply(context: str, message: str) -> str:
    return (
        f"Here's what I have on record for this project:\n\n{context}\n\n"
        f"(Note: natural-language chat is running in fallback mode — "
        f"configure GROQ_API_KEY for conversational answers to "
        f"follow-up questions like \"{message}\".)"
    )


def answer(db: Session, project_id: int, message: str) -> tuple[str, bool]:
    context = _build_context(db, project_id)

    if _client is None:
        return _fallback_reply(context, message), False

    system_prompt = (
        "You are the AI Assistant inside a building compliance auditing tool. "
        "Answer the user's question using ONLY the project data provided below. "
        "Do not invent violations, values, or code clauses that aren't listed. "
        "If the data doesn't answer the question, say so plainly.\n\n"
        f"PROJECT DATA:\n{context}"
    )
    try:
        response = _client.chat.completions.create(
            model=settings.groq_model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message},
            ],
            max_tokens=300,
            temperature=0.3,
        )
        return response.choices[0].message.content.strip(), True
    except Exception:
        return _fallback_reply(context, message), False
