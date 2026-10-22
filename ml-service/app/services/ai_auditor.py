import httpx
import os

OLLAMA_URL = os.getenv("OLLAMA_BASE_URL", "http://host.docker.internal:11434")

async def generate_audit_narrative(ref: str, src: str, dest: str, amount: float, anomaly_score: float) -> str:
    """
    Translates mathematical outlier scores into a plain-English compliance narrative.
    Falls back gracefully if external LLM is offline.
    """
    prompt = (
        f"You are a Senior Core-Banking Financial Crime Auditor.\n"
        f"Transaction: {ref}\n"
        f"Source: {src} -> Destination: {dest}\n"
        f"Amount: â‚¹{amount:,.2f}\n"
        f"Isolation Forest Anomaly Score: {anomaly_score}\n"
        f"Explain in 2 concise sentences whether this transaction exceeds standard retail velocity thresholds."
    )

    try:
        async with httpx.AsyncClient(timeout=4.0) as client:
            resp = await client.post(
                f"{OLLAMA_URL}/api/generate",
                json={"model": "qwen2.5-coder", "prompt": prompt, "stream": False}
            )
            if resp.status_code == 200:
                data = resp.json()
                return data.get("response", "").strip()
    except Exception:
        pass

    if amount >= 100000.0:
        return f"High-Value Flag: Transfer of â‚¹{amount:,.2f} exceeds Maker-Checker statutory ceiling (â‚¹1,00,000). Dual authorization required."
    elif anomaly_score > 0.4:
        return f"Velocity Alert: Transfer of â‚¹{amount:,.2f} deviates significantly from account historical baseline (Anomaly Score: {anomaly_score})."
    else:
        return f"Routine Transfer: â‚¹{amount:,.2f} falls within standard retail operating variance."