package com.finshield.audit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String principal;
    private String ipAddress;

    @Column(length = 2000)
    private String details;

    private Instant timestamp = Instant.now();

    public AuditLog(String action, String principal, String ipAddress, String details) {
        this.action = action;
        this.principal = principal;
        this.ipAddress = ipAddress;
        this.details = details;
        this.timestamp = Instant.now();
    }
}