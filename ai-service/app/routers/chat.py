from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db import get_db
from app.schemas import ChatRequest, ChatResponse
from app.llm.chat_engine import answer

router = APIRouter(tags=["chat"])


@router.post("/chat", response_model=ChatResponse)
def chat(request: ChatRequest, db: Session = Depends(get_db)):
    reply, llm_used = answer(db, request.project_id, request.message)
    return ChatResponse(reply=reply, llm_used=llm_used)
