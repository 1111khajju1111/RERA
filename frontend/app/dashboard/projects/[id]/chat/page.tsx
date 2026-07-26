"use client";

import { useEffect, useRef, useState, FormEvent } from "react";
import { useParams } from "next/navigation";
import { Send, Bot, User } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { chatApi } from "@/lib/api";
import type { ChatMessageResponse } from "@/lib/types";
import { cn } from "@/lib/utils";

const SUGGESTED_PROMPTS = [
  "Why might this building be rejected?",
  "Show fire safety violations",
  "How can I improve the approval score?",
  "Summarize all open violations",
];

export default function ChatPage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);

  const [messages, setMessages] = useState<ChatMessageResponse[]>([]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    chatApi.history(projectId).then(setMessages);
  }, [projectId]);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [messages]);

  async function sendMessage(text: string) {
    if (!text.trim() || sending) return;
    setSending(true);
    setInput("");

    // Optimistic user bubble — the real persisted row comes back from the
    // backend along with the assistant reply, but showing it immediately
    // keeps the chat feeling responsive during the LLM round trip.
    const optimisticUser: ChatMessageResponse = {
      id: Date.now(), role: "USER", message: text, createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, optimisticUser]);

    try {
      const assistantReply = await chatApi.send(projectId, text);
      setMessages((prev) => [...prev, assistantReply]);
    } finally {
      setSending(false);
    }
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    sendMessage(input);
  }

  return (
    <div className="mx-auto flex h-[calc(100vh-4rem)] max-w-3xl flex-col">
      <h1 className="mb-4 text-2xl font-semibold">AI Assistant</h1>

      <Card className="flex flex-1 flex-col overflow-hidden">
        <div ref={scrollRef} className="flex-1 space-y-4 overflow-y-auto pr-1">
          {messages.length === 0 && (
            <div className="flex h-full flex-col items-center justify-center text-center text-white/40">
              <Bot className="mb-3 h-8 w-8 text-brand-cyan" />
              <p className="mb-4 text-sm">Ask about this project's compliance status.</p>
              <div className="grid gap-2 sm:grid-cols-2">
                {SUGGESTED_PROMPTS.map((prompt) => (
                  <button
                    key={prompt}
                    onClick={() => sendMessage(prompt)}
                    className="rounded-xl border border-border bg-white/[0.02] px-3 py-2 text-xs text-white/60 hover:bg-white/[0.06] hover:text-white"
                  >
                    {prompt}
                  </button>
                ))}
              </div>
            </div>
          )}

          {messages.map((m) => (
            <div key={m.id} className={cn("flex gap-3", m.role === "USER" && "flex-row-reverse")}>
              <div className={cn(
                "flex h-8 w-8 shrink-0 items-center justify-center rounded-full",
                m.role === "USER" ? "bg-white/[0.08]" : "bg-brand-gradient"
              )}>
                {m.role === "USER" ? <User className="h-4 w-4" /> : <Bot className="h-4 w-4" />}
              </div>
              <div className={cn(
                "max-w-[75%] whitespace-pre-wrap rounded-2xl px-4 py-2.5 text-sm",
                m.role === "USER" ? "bg-white/[0.08]" : "bg-white/[0.03] border border-border"
              )}>
                {m.message}
              </div>
            </div>
          ))}

          {sending && (
            <div className="flex items-center gap-2 text-xs text-white/30">
              <div className="h-3 w-3 animate-spin rounded-full border-2 border-brand-cyan border-t-transparent" />
              Thinking…
            </div>
          )}
        </div>

        <form onSubmit={handleSubmit} className="mt-4 flex gap-2 border-t border-border pt-4">
          <Textarea
            rows={1}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); sendMessage(input); }
            }}
            placeholder="Ask about violations, fixes, or compliance rules…"
            className="flex-1"
          />
          <Button type="submit" disabled={sending || !input.trim()}>
            <Send className="h-4 w-4" />
          </Button>
        </form>
      </Card>
    </div>
  );
}
