from pydantic import BaseModel
from typing import Optional

class FraudEvaluationRequest(BaseModel):
    transactionReference: str
    sourceAccountNumber: str
    destinationAccountNumber: str
    amount: float
    clientIp: Optional[str] = "127.0.0.1"

class FraudEvaluationResponse(BaseModel):
    anomalyScore: float
    isFlagged: bool
    riskLevel: str
    explanation: str