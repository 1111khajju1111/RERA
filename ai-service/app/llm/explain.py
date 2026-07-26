"""
Generates human-readable explanations/suggestions for violations.

Design choice: try the LLM if GROQ_API_KEY is configured; otherwise fall
back to a template that's still grounded in the actual violation data
(not fabricated). This matters for a demo — you don't want the AI
Assistant feature to go blank or error out on stage because of an API
quota, a network hiccup, or a forgotten env var.

Provider: Groq (not OpenAI) — Groq's Python SDK mirrors OpenAI's
chat-completions shape closely, so this is a near drop-in swap. Groq's
free tier has tighter rate limits than OpenAI's paid tier (roughly
30 requests/minute, 1000 requests/day on gpt-oss-120b at time of writing)
— fine for a demo, worth checking console.groq.com/docs/rate-limits
before relying on this under real traffic.
"""

from app.config import settings
from app.models import Violation

_client = None
if settings.groq_api_key:
    from groq import Groq
    _client = Groq(api_key=settings.groq_api_key)


def _template_suggestion(violation: Violation) -> str:
    return (
        f"{violation.severity.title()} issue: {violation.description} "
        f"Recommended fix: adjust the relevant element so the measured value "
        f"({violation.detected_value}) meets the required threshold "
        f"({violation.required_value}). Consult the cited code clause for "
        f"the exact compliant range before resubmitting."
    )


def generate_suggestion(violation: Violation) -> tuple[str, bool]:
    """Returns (suggestion_text, llm_used)."""
    if _client is None:
        return _template_suggestion(violation), False

    prompt = (
        f"You are a building compliance assistant. A violation was detected:\n"
        f"Rule: {violation.description}\n"
        f"Detected value: {violation.detected_value}\n"
        f"Required value: {violation.required_value}\n"
        f"Severity: {violation.severity}\n\n"
        f"In 2-3 concise sentences, explain the practical design fix an "
        f"architect should make. Be specific and actionable, not generic."
    )
    try:
        response = _client.chat.completions.create(
            model=settings.groq_model,
            messages=[{"role": "user", "content": prompt}],
            max_tokens=200,
            temperature=0.4,
        )
        return response.choices[0].message.content.strip(), True
    except Exception:
        # Never let an LLM/network failure break the pipeline — fall back silently.
        return _template_suggestion(violation), False
