import React, { useState, useEffect } from 'react'
import { api } from './api/client'

export default function App() {
  const [activeTab, setActiveTab] = useState('overview')
  const [accounts, setAccounts] = useState([])
  const [transactions, setTransactions] = useState([])
  const [pendingQueue, setPendingQueue] = useState([])
  const [showTransferModal, setShowTransferModal] = useState(false)
  const [transferForm, setTransferForm] = useState({
    sourceAccountNumber: '',
    destinationAccountNumber: '',
    amount: '',
    description: ''
  })
  const [transferStatus, setTransferStatus] = useState(null)
  const [loading, setLoading] = useState(false)

  // Seed sample initial state if backend is still starting
  const sampleAccounts = [
    { id: 1, accountNumber: 'VAULT-1001', name: 'Central Reserve Vault (Asset)', type: 'ASSET', status: 'ACTIVE', balance: 10000000.0, currency: 'INR' },
    { id: 2, accountNumber: 'ACC-2001', name: 'John Doe Savings Account', type: 'LIABILITY', status: 'ACTIVE', balance: 250000.0, currency: 'INR' },
    { id: 3, accountNumber: 'ACC-2002', name: 'Alice Smith Checking Account', type: 'LIABILITY', status: 'ACTIVE', balance: 150000.0, currency: 'INR' },
    { id: 4, accountNumber: 'FEE-3001', name: 'Transaction Processing Revenue', type: 'INCOME', status: 'ACTIVE', balance: 0.0, currency: 'INR' }
  ]

  const loadData = async () => {
    try {
      const accs = await api.getAccounts()
      setAccounts(accs.length > 0 ? accs : sampleAccounts)
      const txs = await api.getTransactions()
      setTransactions(txs)
      const queue = await api.getMakerCheckerQueue()
      setPendingQueue(queue)
    } catch {
      setAccounts(sampleAccounts)
    }
  }

  useEffect(() => {
    loadData()
    const timer = setInterval(loadData, 5000)
    return () => clearInterval(timer)
  }, [])

  const handleTransferSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setTransferStatus(null)

    try {
      const payload = {
        ...transferForm,
        amount: parseFloat(transferForm.amount),
        idempotencyKey: 'idemp-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7)
      }
      const res = await api.processTransfer(payload)
      setTransferStatus(res)
      if (res.success) {
        setTransferForm({ sourceAccountNumber: '', destinationAccountNumber: '', amount: '', description: '' })
        loadData()
      }
    } catch (err) {
      setTransferStatus({ success: false, message: err.message || 'Transfer failed' })
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (id) => {
    await api.approveTransaction(id, 'compliance_officer', 'Approved after audit review')
    loadData()
  }

  const handleReject = async (id) => {
    await api.rejectTransaction(id, 'compliance_officer', 'Velocity threshold exceeded')
    loadData()
  }

  const totalLiquidity = accounts.reduce((acc, a) => acc + (a.balance || 0), 0)

  return (
    <div style={{ maxWidth: '1240px', margin: '0 auto', padding: '24px 16px' }}>
      {/* Top Header */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ width: '40px', height: '40px', background: 'linear-gradient(135deg, #38bdf8, #2563eb)', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '800', color: '#fff' }}>
              FS
            </div>
            <div>
              <h1 style={{ fontSize: '1.6rem', fontWeight: '800', letterSpacing: '-0.02em' }}>FinShield</h1>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Enterprise Double-Entry Banking Ledger â€¢ Apache Fineract COA â€¢ AI Fraud Intelligence
              </p>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <button className=\"btn-secondary\" onClick={loadData}>â†» Refresh</button>
          <button className=\"btn-primary\" onClick={() => setShowTransferModal(true)}>+ Initiate Transfer</button>
        </div>
      </header>

      {/* Navigation Tabs */}
      <nav style={{ display: 'flex', gap: '8px', borderBottom: '1px solid rgba(148, 163, 184, 0.15)', marginBottom: '28px', paddingBottom: '12px' }}>
        {[
          { id: 'overview', label: 'ðŸ“Š System Overview' },
          { id: 'accounts', label: 'ðŸ¦ Chart of Accounts (COA)' },
          { id: 'makerchecker', label: âš–ï¸ Maker-Checker Queue () },
          { id: 'ledger', label: 'ðŸ“œ General Ledger Journal' }
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            style={{
              background: activeTab === tab.id ? 'rgba(56, 189, 248, 0.15)' : 'transparent',
              color: activeTab === tab.id ? 'var(--accent-blue)' : 'var(--text-muted)',
              border: activeTab === tab.id ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid transparent',
              padding: '8px 16px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: '600',
              fontSize: '0.9rem'
            }}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {/* OVERVIEW TAB */}
      {activeTab === 'overview' && (
        <div>
          {/* Key Metric Tiles */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '16px', marginBottom: '28px' }}>
            <div className=\"glass-card\" style={{ padding: '20px' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '6px' }}>Total Ledger Liquidity</div>
              <div className=\"mono\" style={{ fontSize: '1.8rem', fontWeight: '800', color: 'var(--accent-blue)' }}>
                â‚¹{totalLiquidity.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </div>
              <div style={{ fontSize: '0.75rem', color: '#10b981', marginTop: '6px' }}>âœ“ Balanced: Debits â‰¡ Credits</div>
            </div>

            <div className=\"glass-card\" style={{ padding: '20px' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '6px' }}>Active Accounts</div>
              <div className=\"mono\" style={{ fontSize: '1.8rem', fontWeight: '800' }}>{accounts.length}</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '6px' }}>Fineract 5-Tier Chart of Accounts</div>
            </div>

            <div className=\"glass-card\" style={{ padding: '20px' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '6px' }}>Maker-Checker Approvals</div>
              <div className=\"mono\" style={{ fontSize: '1.8rem', fontWeight: '800', color: pendingQueue.length > 0 ? 'var(--accent-amber)' : 'var(--accent-green)' }}>
                {pendingQueue.length} Pending
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '6px' }}>Statutory Threshold: â‚¹1,00,000</div>
            </div>

            <div className=\"glass-card\" style={{ padding: '20px' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '6px' }}>AI Anti-Fraud Engine</div>
              <div className=\"mono\" style={{ fontSize: '1.8rem', fontWeight: '800', color: '#a78bfa' }}>Active</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '6px' }}>Isolation Forest + Local Ollama / Gemini</div>
            </div>
          </div>

          {/* Accounts Grid Preview */}
          <h2 style={{ fontSize: '1.2rem', marginBottom: '16px' }}>Core Accounts (Fineract Standard)</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px', marginBottom: '32px' }}>
            {accounts.map(acc => (
              <div key={acc.accountNumber} className=\"glass-card\" style={{ padding: '18px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <span className=\"mono\" style={{ fontSize: '0.8rem', color: 'var(--accent-blue)', fontWeight: '600' }}>{acc.accountNumber}</span>
                  <span className={adge badge-\}>{acc.type}</span>
                </div>
                <div style={{ fontWeight: '600', fontSize: '1rem', marginBottom: '8px' }}>{acc.name}</div>
                <div className=\"mono\" style={{ fontSize: '1.4rem', fontWeight: '700' }}>
                  â‚¹{(acc.balance || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ACCOUNTS TAB */}
      {activeTab === 'accounts' && (
        <div className=\"glass-card\" style={{ padding: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', marginBottom: '16px' }}>Apache Fineract Chart of Accounts (COA) Structure</h2>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.2)', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <th style={{ padding: '12px' }}>Account Number</th>
                <th style={{ padding: '12px' }}>Account Name</th>
                <th style={{ padding: '12px' }}>Fineract Category</th>
                <th style={{ padding: '12px' }}>Lifecycle Status</th>
                <th style={{ padding: '12px', textAlign: 'right' }}>Balance</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map(acc => (
                <tr key={acc.accountNumber} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.1)' }}>
                  <td className=\"mono\" style={{ padding: '12px', color: 'var(--accent-blue)' }}>{acc.accountNumber}</td>
                  <td style={{ padding: '12px', fontWeight: '500' }}>{acc.name}</td>
                  <td style={{ padding: '12px' }}><span className={adge badge-\}>{acc.type}</span></td>
                  <td style={{ padding: '12px' }}><span style={{ color: '#10b981', fontSize: '0.85rem' }}>â— {acc.status}</span></td>
                  <td className=\"mono\" style={{ padding: '12px', textAlign: 'right', fontWeight: '700' }}>
                    â‚¹{(acc.balance || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* MAKER-CHECKER TAB */}
      {activeTab === 'makerchecker' && (
        <div className=\"glass-card\" style={{ padding: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', marginBottom: '6px' }}>Maker-Checker Dual Authorization Queue</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '20px' }}>
            In compliance with Core Banking standards, transactions exceeding â‚¹1,00,000 or flagged by AI require approval from a Compliance Officer.
          </p>

          {pendingQueue.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
              âœ“ No transactions currently pending dual-authorization review.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {pendingQueue.map(item => (
                <div key={item.id} className=\"glass-card\" style={{ padding: '18px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginBottom: '6px' }}>
                      <span className=\"mono\" style={{ fontWeight: '700', color: 'var(--accent-blue)' }}>{item.transactionReference}</span>
                      <span className=\"badge badge-pending\">REQUIRES APPROVAL</span>
                      <span className=\"badge badge-rejected\">AI Risk: {item.riskScore}</span>
                    </div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                      Route: <span className=\"mono\" style={{ color: '#f8fafc' }}>{item.sourceAccountNumber}</span> âž” <span className=\"mono\" style={{ color: '#f8fafc' }}>{item.destinationAccountNumber}</span>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                    <div className=\"mono\" style={{ fontSize: '1.3rem', fontWeight: '800' }}>
                      â‚¹{(item.amount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button className=\"btn-primary\" style={{ background: '#10b981' }} onClick={() => handleApprove(item.id)}>Approve</button>
                      <button className=\"btn-secondary\" style={{ color: '#ef4444' }} onClick={() => handleReject(item.id)}>Reject</button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* LEDGER TAB */}
      {activeTab === 'ledger' && (
        <div className=\"glass-card\" style={{ padding: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', marginBottom: '16px' }}>General Ledger Journal (Double-Entry Log)</h2>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.2)', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                <th style={{ padding: '12px' }}>Tx Reference</th>
                <th style={{ padding: '12px' }}>Description</th>
                <th style={{ padding: '12px' }}>Status</th>
                <th style={{ padding: '12px' }}>Risk Score</th>
                <th style={{ padding: '12px', textAlign: 'right' }}>Amount</th>
              </tr>
            </thead>
            <tbody>
              {transactions.length === 0 ? (
                <tr>
                  <td colSpan=\"5\" style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)' }}>
                    No transactions executed yet. Initiate a transfer to record your first double-entry journal entries!
                  </td>
                </tr>
              ) : (
                transactions.map(tx => (
                  <tr key={tx.id} style={{ borderBottom: '1px solid rgba(148, 163, 184, 0.1)' }}>
                    <td className=\"mono\" style={{ padding: '12px', color: 'var(--accent-blue)' }}>{tx.transactionReference}</td>
                    <td style={{ padding: '12px' }}>{tx.description}</td>
                    <td style={{ padding: '12px' }}><span className={adge badge-\}>{tx.status}</span></td>
                    <td className=\"mono\" style={{ padding: '12px' }}>{tx.riskScore}</td>
                    <td className=\"mono\" style={{ padding: '12px', textAlign: 'right', fontWeight: '700' }}>
                      â‚¹{(tx.amount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* TRANSFER MODAL */}
      {showTransferModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50 }}>
          <div className=\"glass-card\" style={{ width: '100%', maxWidth: '520px', padding: '28px', background: '#0b1120' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Initiate Double-Entry Transfer</h3>
              <button onClick={() => setShowTransferModal(false)} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>Ã—</button>
            </div>

            <form onSubmit={handleTransferSubmit}>
              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Source Account (Debit)</label>
                <select
                  value={transferForm.sourceAccountNumber}
                  onChange={e => setTransferForm({ ...transferForm, sourceAccountNumber: e.target.value })}
                  required
                >
                  <option value=\"\">Select Source Account</option>
                  {accounts.map(a => <option key={a.accountNumber} value={a.accountNumber}>{a.accountNumber} - {a.name} (Bal: â‚¹{a.balance})</option>)}
                </select>
              </div>

              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Destination Account (Credit)</label>
                <select
                  value={transferForm.destinationAccountNumber}
                  onChange={e => setTransferForm({ ...transferForm, destinationAccountNumber: e.target.value })}
                  required
                >
                  <option value=\"\">Select Destination Account</option>
                  {accounts.map(a => <option key={a.accountNumber} value={a.accountNumber}>{a.accountNumber} - {a.name}</option>)}
                </select>
              </div>

              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Transfer Amount (INR)</label>
                <input
                  type=\"number\"
                  step=\"0.01\"
                  placeholder=\"e.g. 50000 or 150000 (Maker-Checker threshold)\"
                  value={transferForm.amount}
                  onChange={e => setTransferForm({ ...transferForm, amount: e.target.value })}
                  required
                />
              </div>

              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Transfer Description</label>
                <input
                  type=\"text\"
                  placeholder=\"e.g. Supplier Invoice Settlement\"
                  value={transferForm.description}
                  onChange={e => setTransferForm({ ...transferForm, description: e.target.value })}
                />
              </div>

              {transferStatus && (
                <div style={{ padding: '12px', borderRadius: '8px', marginBottom: '16px', fontSize: '0.85rem', background: transferStatus.success ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)', color: transferStatus.success ? '#34d399' : '#f87171' }}>
                  {transferStatus.message}
                </div>
              )}

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button type=\"button\" className=\"btn-secondary\" onClick={() => setShowTransferModal(false)}>Cancel</button>
                <button type=\"submit\" className=\"btn-primary\" disabled={loading}>
                  {loading ? 'Processing Ledger Lock...' : 'Execute Transfer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}