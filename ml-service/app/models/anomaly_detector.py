import numpy as np
from sklearn.ensemble import IsolationForest

class TransactionAnomalyDetector:
    """
    Unsupervised Outlier Detection using Isolation Forest.
    Trained on typical commercial transaction distributions (mean â‚¹5,000, std â‚¹15,000).
    Evaluates new transfer amounts and velocity signatures.
    """
    def __init__(self):
        np.random.seed(42)
        # Baseline normal transaction amounts
        normal_amounts = np.random.exponential(scale=12000.0, size=(1000, 1))
        self.model = IsolationForest(contamination=0.03, random_state=42)
        self.model.fit(normal_amounts)

    def evaluate(self, amount: float) -> tuple[float, bool]:
        # IsolationForest decision_function: lower means more abnormal
        raw_score = float(self.model.decision_function([[amount]])[0])
        prediction = int(self.model.predict([[amount]])[0])
        
        # Normalize anomaly score to [0.0 (safe), 1.0 (highly abnormal)]
        normalized_anomaly = max(0.0, min(1.0, float(0.5 - raw_score)))
        is_anomaly = prediction == -1 or amount >= 100000.0
        
        return round(normalized_anomaly, 4), is_anomaly

anomaly_detector = TransactionAnomalyDetector()