<div align=\"center\">

# ðŸ›¡ï¸ FinShield
### Enterprise High-Throughput Double-Entry Ledger & AI Fraud Intelligence Platform
**Architected as an Enterprise Modular Monolith following Apache Fineract & Mifos X Standards**

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.2-brightgreen.svg?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Apache Fineract](https://img.shields.io/badge/Domain-Apache_Fineract_COA-blue.svg?style=for-the-badge&logo=apache&logoColor=white)](https://fineract.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0_ACID-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0_Idempotency-DC382D.svg?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![FastAPI](https://img.shields.io/badge/FastAPI-Python_3.10-009688.svg?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![React](https://img.shields.io/badge/React-18_Vite-61DAFB.svg?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)

<p align=\"center\">
  <b>A production-grade core banking modular monolith combining strict financial double-entry accounting (Debits â‰¡ Credits), deadlock-free pessimistic locking, Maker-Checker dual authorization, and machine learning fraud detection.</b>
</p>

</div>

---

## ðŸ“Œ Executive Summary

**FinShield** is an enterprise-grade core banking transaction and ledger platform designed using the domain standards of **Apache Fineract** and **Mifos X**â€”the global open-source core banking engines used by digital banks and microfinance institutions worldwide.

Unlike distributed microservices which introduce eventual consistency lags and complex distributed transactions (2PC / Sagas) for ledger entries, **FinShield is intentionally built as a Modular Monolith**. This provides:
- **Sub-millisecond latency** with zero network hop overhead between ledger and account modules.
- **Strict ACID atomicity**: Money is never created or destroyed; every transfer commits or rolls back across debit and credit journal entries simultaneously.
- **Deadlock-Free Concurrency**: Row-level pessimistic locking (SELECT ... FOR UPDATE) ordered by account number prevents concurrent double-spend race conditions.
- **Statutory Compliance**: Enforces the **Maker-Checker (Dual Control)** rule on transfers exceeding â‚¹1,00,000.
- **AI Fraud Scoring**: An asynchronous sidecar powered by scikit-learn's IsolationForest and local LLM auditors (qwen2.5-coder / Gemini).

---

## ðŸ—ï¸ System Architecture

`mermaid
flowchart TB
    subgraph ClientLayer[\"ðŸ–¥ï¸ Client & Operations Dashboard\"]
        UI[\"React 18 Dashboard<br/>(Accounts, Maker-Checker Queue, Live Journal)\"]
    end

    subgraph MonolithCore[\"ðŸ¢ FinShield Modular Monolith (Spring Boot 3)\"]
        SEC[\"Spring Security + JWT + RBAC<br/>(CUSTOMER, COMPLIANCE_OFFICER, ADMIN)\"]
        GQL[\"Spring GraphQL Controller<br/>(Nested Account & Ledger Tree Queries)\"]
        
        subgraph Domains[\"Decoupled Hexagonal Domain Modules\"]
            ACC[\"Account Module<br/>(Fineract COA & Lifecycle Machine)\"]
            LEDGER[\"Double-Entry Engine<br/>(Atomic Debits â‰¡ Credits)\"]
            MC[\"Maker-Checker Service<br/>(Dual Authorization Workflow)\"]
            AUDIT[\"Audit Trail Module<br/>(IP, Device, Timestamp Logging)\"]
        end
    end

    subgraph StorageLayer[\"ðŸ’¾ Multi-Model Persistence\"]
        MYSQL[(\"MySQL 8.0 / H2<br/>Accounts & Journal Entries<br/>(Pessimistic Row Locks)\")]
        REDIS[(\"Redis 7.0<br/>Idempotency Keys & Token Bucket Rate Limiting\")]
        MONGO[(\"MongoDB 6.0<br/>Dynamic Regulatory Audit Records\")]
    end

    subgraph AIService[\"ðŸ§  Machine Learning Fraud Sidecar\"]
        FASTAPI[\"Python FastAPI (Port 8000)\"]
        ISOF[\"Isolation Forest Anomaly Model\"]
        LLM[\"Local Ollama / Gemini API<br/>(Natural Language Audit Narratives)\"]
    end

    UI <== \"REST API & GraphQL\" ==> SEC
    SEC ==> GQL
    SEC ==> LEDGER
    SEC ==> MC

    LEDGER <--> REDIS
    LEDGER == \"Pessimistic Lock (Deadlock-Free)\" ==> MYSQL
    LEDGER -. \"Async Audit Event\" .-> AUDIT
    AUDIT --> MONGO

    LEDGER -. \"Fraud Evaluation (< 50ms)\" .-> FASTAPI
    FASTAPI --> ISOF
    ISOF --> LLM
    LLM -. \"Risk Score & Narrative\" .-> LEDGER
`

---

## ðŸ›ï¸ Apache Fineract Chart of Accounts (COA) Model

FinShield models its account hierarchy according to the 5 standard Fineract financial classes:

| Class | Fineract Category | Normal Balance | Example Accounts in FinShield |
| :--- | :--- | :--- | :--- |
| **1000** | **ASSET** | **DEBIT** | VAULT-1001 (Central Reserve Vault, Liquidity Reserves) |
| **2000** | **LIABILITY** | **CREDIT** | ACC-2001 (Customer Savings), ACC-2002 (Customer Checking) |
| **3000** | **EQUITY** | **CREDIT** | Retained Earnings, Shareholder Capital |
| **4000** | **INCOME** | **CREDIT** | FEE-3001 (Transaction Processing Revenue) |
| **5000** | **EXPENSE** | **DEBIT** | Network Gateway Charges, Operating Costs |

### The Double-Entry Rule
\sum \text{Debits} = \sum \text{Credits}
When Customer A transfers â‚¹25,000 to Customer B:
1. **Debit**: Customer A Deposit Account (Liability decreases by â‚¹25,000).
2. **Credit**: Customer B Deposit Account (Liability increases by â‚¹25,000).
*Total balance across the system remains strictly invariant.*

---

## âš–ï¸ Maker-Checker (Dual Authorization) Workflow

`mermaid
sequenceDiagram
    autonumber
    actor Maker as ðŸ§‘â€ðŸ’¼ Maker (Customer / Teller)
    participant Core as ðŸ¢ FinShield Core
    participant AI as ðŸ§  ML Fraud Sidecar
    participant DB as ðŸ—„ï¸ MySQL Ledger
    actor Checker as ðŸ•µï¸ Checker (Compliance Officer)

    Maker->>Core: Initiate Transfer (â‚¹1,50,000)
    Core->>AI: Evaluate Anomaly Score
    AI-->>Core: Risk Score: 0.65 (High Value Flag)
    Note over Core: Amount >= â‚¹1,00,000 threshold triggered
    Core->>DB: Save JournalTransaction (Status: PENDING_APPROVAL)
    Core-->>Maker: Response: \"Held for Maker-Checker review\"

    Note over Core,Checker: High-Value Review Queue
    Checker->>Core: View Pending Review Queue
    Checker->>Core: POST /api/v1/maker-checker/{id}/approve
    Core->>DB: Lock Accounts, Apply Debits & Credits
    Core->>DB: Update Status: APPROVED
    Core-->>Checker: Double-entry successfully committed
`

---

## ðŸš€ Quickstart & Setup

### Option 1: Run with Docker Compose (Recommended)
`ash
git clone https://github.com/Shailendra0320/finshield.git
cd finshield
docker compose up -d
`
- **Frontend Dashboard**: http://localhost:3000
- **Backend Core**: http://localhost:8080
- **GraphQL Playground**: http://localhost:8080/graphiql
- **H2 Database Console**: http://localhost:8080/h2-console
- **AI Fraud Engine**: http://localhost:8000/docs

---

## ðŸ‘¨â€ðŸ’» Author & Sole Contributor

<div align=\"center\">

**Shailendra Vishwakarma**  
*Software Developer | Core Banking & Distributed Systems Specialist*  
Email: [12654vishwakarmashailendra7@gmail.com](mailto:12654vishwakarmashailendra7@gmail.com) â€¢ [LinkedIn](https://linkedin.com) â€¢ [GitHub](https://github.com/Shailendra0320)

</div>