package com.finshield.makerchecker.controller;

import com.finshield.common.response.ApiResponse;
import com.finshield.ledger.dto.TransferResponse;
import com.finshield.ledger.model.JournalTransaction;
import com.finshield.makerchecker.service.MakerCheckerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/maker-checker")
public class MakerCheckerController {

    private final MakerCheckerService makerCheckerService;

    public MakerCheckerController(MakerCheckerService makerCheckerService) {
        this.makerCheckerService = makerCheckerService;
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<JournalTransaction>>> getPendingQueue() {
        return ResponseEntity.ok(ApiResponse.ok("Pending authorization queue", makerCheckerService.getPendingQueue()));
    }

    @PostMapping("/{transactionId}/approve")
    public ResponseEntity<ApiResponse<TransferResponse>> approve(
            @PathVariable Long transactionId,
            @RequestBody(required = false) Map<String, String> body) {
        String reviewer = (body != null && body.containsKey("reviewer")) ? body.get("reviewer") : "compliance_officer";
        String comments = (body != null && body.containsKey("comments")) ? body.get("comments") : "Approved by compliance";
        return ResponseEntity.ok(ApiResponse.ok("Transaction approved", makerCheckerService.approveTransaction(transactionId, reviewer, comments)));
    }

    @PostMapping("/{transactionId}/reject")
    public ResponseEntity<ApiResponse<TransferResponse>> reject(
            @PathVariable Long transactionId,
            @RequestBody(required = false) Map<String, String> body) {
        String reviewer = (body != null && body.containsKey("reviewer")) ? body.get("reviewer") : "compliance_officer";
        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Policy violation";
        return ResponseEntity.ok(ApiResponse.ok("Transaction rejected", makerCheckerService.rejectTransaction(transactionId, reviewer, reason)));
    }
}