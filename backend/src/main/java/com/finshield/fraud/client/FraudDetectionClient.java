package com.finshield.fraud.client;

import com.finshield.fraud.dto.FraudCheckRequest;
import com.finshield.fraud.dto.FraudCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.Duration;

@Component
public class FraudDetectionClient {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionClient.class);
    private final WebClient webClient;

    public FraudDetectionClient(
            WebClient.Builder webClientBuilder,
            @Value("${finshield.ml.fraud-service-url:http://localhost:8000}") String fraudServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(fraudServiceUrl).build();
    }

    public FraudCheckResponse evaluateRisk(FraudCheckRequest request) {
        try {
            return webClient.post()
                    .uri("/api/v1/fraud/evaluate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FraudCheckResponse.class)
                    .timeout(Duration.ofMillis(800))
                    .block();
        } catch (Exception ex) {
            log.warn("ML sidecar unavailable, falling back to heuristic engine: {}", ex.getMessage());
            boolean highAmount = request.amount().compareTo(new BigDecimal("100000")) >= 0;
            return new FraudCheckResponse(
                    highAmount ? 0.75 : 0.05,
                    highAmount,
                    highAmount ? "HIGH_RISK" : "LOW_RISK",
                    highAmount ? "Heuristic rule triggered: Transfer exceeds INR 100,000 threshold." : "Transaction within baseline statistical limits."
            );
        }
    }
}