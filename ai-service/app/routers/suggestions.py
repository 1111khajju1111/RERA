from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db import get_db
from app.schemas import GenerateSuggestionsRequest, GenerateSuggestionsResponse
from app.models import Violation, AiSuggestion
from app.llm.explain import generate_suggestion

router = APIRouter(tags=["suggestions"])


@router.post("/generate-suggestions", response_model=GenerateSuggestionsResponse)
def generate_suggestions(request: GenerateSuggestionsRequest, db: Session = Depends(get_db)):
    violations = db.query(Violation).filter(
        Violation.project_id == request.project_id, Violation.status == "OPEN"
    ).all()

    # Clear old suggestions for this project so re-running doesn't duplicate them.
    db.query(AiSuggestion).filter(AiSuggestion.project_id == request.project_id).delete()

    llm_used_any = False
    for violation in violations:
        text, llm_used = generate_suggestion(violation)
        llm_used_any = llm_used_any or llm_used
        db.add(AiSuggestion(
            project_id=request.project_id,
            violation_id=violation.id,
            suggestion_text=text,
            category=violation.rule.category if violation.rule else "GENERAL",
        ))

    db.commit()

    return GenerateSuggestionsResponse(
        project_id=request.project_id,
        suggestions_created=len(violations),
        llm_used=llm_used_any,
    )
