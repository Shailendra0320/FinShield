from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.schemas.dto import FraudEvaluationRequest, FraudEvaluationResponse
from app.models.anomaly_detector import anomaly_detector
from app.services.ai_auditor import generate_audit_narrative

app = FastAPI(
    title="FinShield AI Fraud Intelligence Service",
    description="Real-Time Isolation Forest Anomaly Detection and AI Compliance Auditing",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health")
def health():
    return {"status": "UP", "service": "finshield-ml"}

@app.post("/api/v1/fraud/evaluate", response_model=FraudEvaluationResponse)
async def evaluate_fraud(payload: FraudEvaluationRequest):
    anomaly_score, is_flagged = anomaly_detector.evaluate(payload.amount)
    risk_level = "CRITICAL" if anomaly_score > 0.7 or payload.amount >= 500000 else ("HIGH" if is_flagged else "LOW")
    explanation = await generate_audit_narrative(
        payload.transactionReference,
        payload.sourceAccountNumber,
        payload.destinationAccountNumber,
        payload.amount,
        anomaly_score
    )

    return FraudEvaluationResponse(
        anomalyScore=anomaly_score,
        isFlagged=is_flagged,
        riskLevel=risk_level,
        explanation=explanation
    )