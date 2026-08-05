"""
Real geocoding via OpenStreetMap's Nominatim — no API key needed, but
read the usage policy before relying on this beyond a demo:
https://operations.osmfoundation.org/policies/nominatim/

Key constraints of the public instance (nominatim.openstreetmap.org):
  - Max 1 request/second
  - A valid, identifying User-Agent header is REQUIRED — a generic or
    placeholder one gets blocked outright, independent of anything else
  - No heavy/bulk/production use — self-host Nominatim or use a paid
    geocoder (Google, Mapbox, LocationIQ) for anything beyond a hackathon
    demo or light internal tool. In particular, requests from cloud/
    datacenter IP ranges (Render, AWS, GCP, ...) are known to get 403'd
    by the public instance's abuse protection — this is a hosting-
    provider-level block, not something a better User-Agent fixes on its
    own. If GeocodeError below reports a 403, that's almost certainly
    what's happening; the real fix is switching to a paid geocoder (e.g.
    LocationIQ has a generous free tier and doesn't apply this block).

This module respects the rate limit with a simple sleep. It does NOT
implement retry/backoff or caching — worth adding before any real usage
volume.
"""

import os
import time
import httpx

NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"

# Nominatim's policy requires a *genuinely identifying* User-Agent — a
# generic or placeholder one is grounds for blocking on its own. Set
# GEOCODER_CONTACT_EMAIL in the environment to your real contact info;
# this fallback is still descriptive but you should not ship on it.
_CONTACT = os.environ.get("GEOCODER_CONTACT_EMAIL", "unset-see-GEOCODER_CONTACT_EMAIL-env-var")
USER_AGENT = f"ai-rera-auditor/0.1 ({_CONTACT})"

_last_request_time = 0.0


class GeocodeError(Exception):
    """Raised when Nominatim itself fails (as opposed to a clean 'no results')."""
    def __init__(self, message: str, status_code: int | None = None):
        super().__init__(message)
        self.status_code = status_code


def _respect_rate_limit():
    global _last_request_time
    elapsed = time.time() - _last_request_time
    if elapsed < 1.0:
        time.sleep(1.0 - elapsed)
    _last_request_time = time.time()


def geocode_address(address: str) -> dict | None:
    """
    Returns {"latitude": float, "longitude": float, "display_name": str},
    or None if Nominatim understood the query but found no match.

    Raises GeocodeError if Nominatim itself couldn't be reached or
    rejected the request (network error, timeout, 403/429/5xx) — this is
    distinct from "no results found" and the caller should surface it
    differently (it's not something the user can fix by trying a
    different address).
    """
    _respect_rate_limit()

    try:
        response = httpx.get(
            NOMINATIM_URL,
            params={"q": address, "format": "json", "limit": 1},
            headers={"User-Agent": USER_AGENT},
            timeout=10.0,
        )
        response.raise_for_status()
    except httpx.HTTPStatusError as e:
        status = e.response.status_code
        if status == 403:
            raise GeocodeError(
                "Nominatim returned 403 Forbidden — this almost always means the "
                "request came from a blocked cloud/datacenter IP range (see module "
                "docstring), not a bad address. Consider switching to a paid geocoder "
                "such as LocationIQ for hosted deployments.",
                status_code=403,
            ) from e
        if status == 429:
            raise GeocodeError("Nominatim rate limit exceeded (max 1 request/second).", status_code=429) from e
        raise GeocodeError(f"Nominatim returned HTTP {status}.", status_code=status) from e
    except httpx.RequestError as e:
        raise GeocodeError(f"Could not reach Nominatim: {e}") from e

    results = response.json()

    if not results:
        return None

    result = results[0]
    return {
        "latitude": float(result["lat"]),
        "longitude": float(result["lon"]),
        "display_name": result["display_name"],
    }
