-- ============================================================
-- V7: Bearer token authentication
--
-- Replaces session-cookie auth. Frontend (Vercel) and backend
-- (Render) are on unrelated domains, so the session cookie was
-- classified as "third-party" by browsers — Safari/Firefox/Brave
-- block third-party cookies unconditionally, and even Chrome blocks
-- them in Incognito and for any user who's toggled the privacy
-- setting. No cookie attribute configuration (SameSite=None; Secure)
-- can fix that classification. A bearer token sent in a normal
-- Authorization header isn't a cookie at all, so it isn't subject to
-- any cookie policy in any browser.
-- ============================================================

CREATE TABLE auth_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_auth_tokens_token ON auth_tokens(token);
