# Updated project — full sync notes

Your sandbox environment resets can happen mid-session on my end too — it
did here, which is why this arrives as one complete updated copy of
`RERA/`, `ai-service/`, and `frontend/` rather than another small patch.
This folder reflects **everything** built across our conversation:

## 1. Deploy fixes (should already match your live deployment)
- `AiReraAuditorApplication.java` — Dotenv code removed
- `ai-service/Dockerfile` — binds to Render's `$PORT`
- `SecurityConfig.java` — CORS origin list, no trailing slash

## 2. Auth: session cookies → bearer tokens (NEW since your last deploy)
Cross-domain (Vercel ↔ Render) session cookies get blocked by browser
privacy policies no matter how they're configured. Replaced with a
bearer token in the `Authorization` header — see `V7__auth_tokens.sql`
and the rewritten `AuthService`/`SecurityConfig`/`AuthController`
(backend) and `token-storage.ts`/`api.ts`/`auth-store.ts` (frontend).

**Includes a bug fix within this fix**: the first version of this had a
`LazyInitializationException` in `BearerTokenAuthenticationFilter` —
`AuthTokenRepository.findByToken` now uses `JOIN FETCH` to eagerly load
the user, which is what actually made login work end-to-end.

`application.properties` also had `jwt.secret`/`jwt.expiration` (unused,
dead, a secret sitting in a committed file) and the now-meaningless
session-cookie lines removed, plus `server.forward-headers-strategy=framework`
added (correct scheme detection behind Render's reverse proxy).

## 3. Project Timeline / Version History (NEW — Phase A of the upgrade)
- **Real bug fixed first**: re-uploading a revised DXF was silently
  *appending* duplicate rooms/components instead of replacing them —
  every re-upload would have corrupted the compliance score further.
  Fixed in `ai-service/app/cad_parsing/ingest.py`.
- `V8__version_score_linkage.sql` links each compliance score snapshot
  to the specific upload that produced it.
- New page: `/dashboard/projects/[id]/timeline` — every revision, in
  order, with its score, and a "Re-analyze" button per version.
- Scope note (stated honestly in the UI too): this does NOT restore old
  building geometry as the live state — only the file + its score are
  versioned. True geometry rollback is a larger schema change, not done here.

## 4. Minor
- `frontend/versel.json` is misspelled — Vercel only reads `vercel.json`
  exactly. Probably harmless if you set Root Directory via the Vercel
  dashboard instead, but rename it to be safe.

## How to apply
This is a complete, current copy of all three services. Compare against
your repo and copy over what's changed — every file listed above, plus
anything else in this folder that differs from what you have. If you're
comfortable with git, the simplest approach is often to just replace
your working copy of `RERA/`, `ai-service/`, and `frontend/` wholesale
with these folders, then `git diff` to review before committing.

## Redeploy checklist
1. Push backend + ai-service + frontend changes together (mixing old/new
   across services will break things)
2. Confirm Render logs show Flyway applying `V7` and `V8` without error
3. Confirm both Render services show "Started"/"Uvicorn running"
4. Clear browser localStorage (`rera-auth-storage`, `rera_auth_token`) —
   old stored state won't match the new auth mechanism
5. Register or log in fresh, create/upload a project, check Timeline page
