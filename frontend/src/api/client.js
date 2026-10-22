const BASE_URL = '/api/v1'

export const api = {
  // Accounts
  async getAccounts() {
    try {
      const res = await fetch(\${BASE_URL}/accounts\)
      const data = await res.json()
      return data.data || []
    } catch {
      return []
    }
  },

  async createAccount(payload) {
    const res = await fetch(\${BASE_URL}/accounts\, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    return res.json()
  },

  // Ledger & Transfers
  async processTransfer(payload) {
    const res = await fetch(\${BASE_URL}/ledger/transfer\, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    return res.json()
  },

  async getTransactions() {
    try {
      const res = await fetch(\${BASE_URL}/ledger/transactions\)
      const data = await res.json()
      return data.data || []
    } catch {
      return []
    }
  },

  // Maker-Checker
  async getMakerCheckerQueue() {
    try {
      const res = await fetch(\${BASE_URL}/maker-checker/queue\)
      const data = await res.json()
      return data.data || []
    } catch {
      return []
    }
  },

  async approveTransaction(id, reviewer = 'compliance_officer', comments = 'Verified legitimate') {
    const res = await fetch(\${BASE_URL}/maker-checker/\${id}/approve\, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reviewer, comments })
    })
    return res.json()
  },

  async rejectTransaction(id, reviewer = 'compliance_officer', reason = 'Risk threshold exceeded') {
    const res = await fetch(\${BASE_URL}/maker-checker/\${id}/reject\, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reviewer, reason })
    })
    return res.json()
  },

  // Audit Logs
  async getAuditLogs() {
    try {
      const res = await fetch(\${BASE_URL}/audit/logs\)
      const data = await res.json()
      return data.data || []
    } catch {
      return []
    }
  }
}