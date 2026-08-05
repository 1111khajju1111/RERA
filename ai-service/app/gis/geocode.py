"""
Real geocoding via OpenStreetMap's Nominatim — no API key needed, but
read the usage policy before relying on this beyond a demo:
https://operations.osmfoundation.org/policies/nominatim/

Key constraints of the public instance (nominatim.openstreetmap.org):
  - Max 1 request/second
  - A valid, identifying User-Agent header is REQUIRED or you get blocked
  - No heavy/bulk/production use — self-host Nominatim or use a paid
    geocoder (Google, Mapbox, LocationIQ) for anything beyond a hackathon
    demo or light internal tool

This module respects the rate limit with a simple sleep and sets a
descriptive User-Agent. It does NOT implement retry/backoff or caching —
worth adding before any real usage volume.
"""

import time
import httpx

NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"
USER_AGENT = "ai-rera-auditor/0.1 (hackathon project; contact: set-your-email-here)"

_last_request_time = 0.0


def _respect_rate_limit():
    global _last_request_time
    elapsed = time.time() - _last_request_time
    if elapsed < 1.0:
        time.sleep(1.0 - elapsed)
    _last_request_time = time.time()


def geocode_address(address: str) -> dict | None:
    """Returns {"latitude": float, "longitude": float, "display_name": str} or None if not found."""
    _respect_rate_limit()

    response = httpx.get(
        NOMINATIM_URL,
        params={"q": address, "format": "json", "limit": 1},
        headers={"User-Agent": USER_AGENT},
        timeout=10.0,
    )
    response.raise_for_status()
    results = response.json()

    if not results:
        return None

    result = results[0]
    return {
        "latitude": float(result["lat"]),
        "longitude": float(result["lon"]),
        "display_name": result["display_name"],
    }
