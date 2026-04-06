import { useEffect, useMemo, useState } from "react";
import { toast } from "react-hot-toast";
import AppLayout from "../components/AppLayout";
import Loader from "../components/Loader";
import { accountApi, transactionApi } from "../api/bankApi";
import { useAuth } from "../context/AuthContext";

const LARGE_TXN_THRESHOLD = 10000;

function toCsvRow(values) {
  return values
    .map((value) => `"${String(value).replace(/"/g, '""')}"`)
    .join(",");
}

export default function TransactionsPage() {
  const { auth } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [processing, setProcessing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ accountId: "", toAccountId: "", type: "DEPOSIT", amount: 0, description: "" });
  const [filters, setFilters] = useState({ type: "ALL", fromDate: "", toDate: "" });

  const refreshData = async () => {
    setLoading(true);
    try {
      const [accountRes, transactionRes] = await Promise.all([
        accountApi.getAccountsByUser(auth.userId),
        transactionApi.historyByUser(auth.userId)
      ]);
      const fetchedAccounts = accountRes.data || [];
      setAccounts(fetchedAccounts);
      setTransactions(transactionRes.data || []);
      if (fetchedAccounts.length > 0) {
        setForm((prev) => ({
          ...prev,
          accountId: prev.accountId || String(fetchedAccounts[0].id),
          toAccountId: prev.toAccountId || String(fetchedAccounts[0].id)
        }));
      } else {
        setForm((prev) => ({ ...prev, accountId: "", toAccountId: "" }));
      }
    } catch {
      toast.error("Failed to load transactions");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshData();
  }, [auth.userId]);

  const processTransaction = async (event) => {
    event.preventDefault();
    const amount = Number(form.amount);

    if (!form.accountId || Number.isNaN(amount) || amount <= 0) {
      toast.error("Select account and enter valid amount");
      return;
    }

    if (form.type === "TRANSFER_OUT" && !form.toAccountId) {
      toast.error("Select target account for transfer");
      return;
    }

    if (form.type === "WITHDRAW") {
      const confirmed = window.confirm(`Confirm withdrawal of Rs. ${amount.toFixed(2)}?`);
      if (!confirmed) {
        return;
      }
    }

    setProcessing(true);
    try {
      const { data } = await transactionApi.process(
        {
          accountId: Number(form.accountId),
          type: form.type,
          amount,
          toAccountId: form.type === "TRANSFER_OUT" ? Number(form.toAccountId) : null,
          description: form.description || `${form.type} via INBANK`
        },
        auth.userId
      );

      toast.success(data?.status === "FAILED" ? `${form.type} transaction failed` : `${form.type} transaction completed`);
      await refreshData();
      if (amount >= LARGE_TXN_THRESHOLD) toast.success("Large transaction checks completed");
    } catch (error) {
      await refreshData();
      toast.error(error.response?.data?.message || "Transaction failed");
    } finally {
      setProcessing(false);
    }
  };

  const filteredTransactions = useMemo(() => {
    const fromTs = filters.fromDate ? new Date(`${filters.fromDate}T00:00:00`).getTime() : null;
    const toTs = filters.toDate ? new Date(`${filters.toDate}T23:59:59`).getTime() : null;

    return [...transactions]
      .filter((txn) => {
        const txnTs = new Date(txn.timestamp).getTime();
        const typeOk = filters.type === "ALL" || txn.type === filters.type;
        const fromOk = fromTs === null || txnTs >= fromTs;
        const toOk = toTs === null || txnTs <= toTs;
        return typeOk && fromOk && toOk;
      })
      .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
  }, [transactions, filters]);

  const totalBalance = useMemo(
    () => accounts.reduce((sum, account) => sum + Number(account.balance || 0), 0),
    [accounts]
  );

  const analytics = useMemo(() => {
    const data = {
      DEPOSIT: 0,
      WITHDRAW: 0,
      FAILED: 0
    };
    filteredTransactions.forEach((txn) => {
      if ((txn.status || "SUCCESS") === "FAILED") {
        data.FAILED += Number(txn.amount || 0);
      } else if (txn.type === "DEPOSIT") {
        data.DEPOSIT += Number(txn.amount || 0);
      } else if (txn.type === "WITHDRAW") {
        data.WITHDRAW += Number(txn.amount || 0);
      }
    });
    const peak = Math.max(1, data.DEPOSIT, data.WITHDRAW, data.FAILED);
    return {
      values: data,
      peak
    };
  }, [filteredTransactions]);

  const exportCsv = () => {
    if (filteredTransactions.length === 0) {
      toast.error("No transactions available for export");
      return;
    }
    const headers = ["Transaction ID", "Type", "Amount", "Date Time", "Status", "Failure Reason"];
    const rows = filteredTransactions.map((txn) => [
      txn.id,
      txn.type,
      Number(txn.amount).toFixed(2),
      new Date(txn.timestamp).toLocaleString(),
      txn.status || "SUCCESS",
      txn.failureReason || ""
    ]);
    const content = [toCsvRow(headers), ...rows.map(toCsvRow)].join("\n");
    const blob = new Blob([content], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `inbank-transactions-${Date.now()}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <AppLayout>
      <section className="glass panel">
        <h2>Deposit / Withdraw</h2>
        <form className="row g-3" onSubmit={processTransaction}>
          <div className="col-md-3">
            <label className="form-label">Account</label>
            <select
              className="form-select"
              value={form.accountId}
              onChange={(event) => setForm((prev) => ({ ...prev, accountId: event.target.value }))}
              required
            >
              {accounts.map((account) => (
                <option key={account.id} value={account.id}>
                  #{account.id} - {account.accountType}
                </option>
              ))}
            </select>
          </div>

          <div className="col-md-3">
            <label className="form-label">Type</label>
            <select
              className="form-select"
              value={form.type}
              onChange={(event) => setForm((prev) => ({ ...prev, type: event.target.value }))}
            >
              <option value="DEPOSIT">Deposit</option>
              <option value="WITHDRAW">Withdraw</option>
              <option value="TRANSFER_OUT">Transfer</option>
            </select>
          </div>

          {form.type === "TRANSFER_OUT" ? (
            <div className="col-md-3">
              <label className="form-label">To Account</label>
              <select
                className="form-select"
                value={form.toAccountId}
                onChange={(event) => setForm((prev) => ({ ...prev, toAccountId: event.target.value }))}
                required
              >
                {accounts.map((account) => (
                  <option key={`to-${account.id}`} value={account.id}>
                    #{account.id} - {account.accountType}
                  </option>
                ))}
              </select>
            </div>
          ) : null}

          <div className="col-md-3">
            <label className="form-label">Amount</label>
            <input
              className="form-control"
              type="number"
              min="1"
              value={form.amount}
              onChange={(event) => setForm((prev) => ({ ...prev, amount: event.target.value }))}
              required
            />
          </div>

          <div className="col-md-3">
            <label className="form-label">Description</label>
            <input
              className="form-control"
              type="text"
              value={form.description}
              onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
              placeholder="Optional"
            />
          </div>

          <div className="col-12">
            <button className="btn btn-primary" disabled={processing || accounts.length === 0} type="submit">
              {processing ? "Processing..." : "Submit Transaction"}
            </button>
          </div>
        </form>
        {accounts.length === 0 ? <p className="mt-3">No accounts found. Create an account first.</p> : null}
      </section>

      <section className="glass panel mt-4">
        <h3>Transaction History</h3>
        {loading ? (
          <Loader text="Fetching transaction history..." />
        ) : accounts.length === 0 ? (
          <p>No accounts available yet. Create an account to start transacting.</p>
        ) : (
          <>
            <div className="report-toolbar">
              <div className="report-grid">
                <select
                  className="form-select"
                  value={filters.type}
                  onChange={(event) => setFilters((prev) => ({ ...prev, type: event.target.value }))}
                >
                  <option value="ALL">All Types</option>
                  <option value="DEPOSIT">Deposit</option>
                  <option value="WITHDRAW">Withdraw</option>
                  <option value="TRANSFER_IN">Transfer In</option>
                  <option value="TRANSFER_OUT">Transfer Out</option>
                </select>
                <input
                  className="form-control"
                  type="date"
                  value={filters.fromDate}
                  onChange={(event) => setFilters((prev) => ({ ...prev, fromDate: event.target.value }))}
                />
                <input
                  className="form-control"
                  type="date"
                  value={filters.toDate}
                  onChange={(event) => setFilters((prev) => ({ ...prev, toDate: event.target.value }))}
                />
                <button type="button" className="btn btn-outline-primary" onClick={exportCsv}>
                  Download CSV
                </button>
              </div>

              <div className="summary-row">
                <div className="summary-chip">Total Balance: Rs. {totalBalance.toFixed(2)}</div>
                <div className="summary-chip">Records: {filteredTransactions.length}</div>
              </div>
            </div>

            <div className="analytics-grid">
              <div className="metric-block">
                <p>Deposit Volume</p>
                <div className="bar-track">
                  <div className="bar-fill deposit" style={{ width: `${(analytics.values.DEPOSIT / analytics.peak) * 100}%` }} />
                </div>
                <strong>Rs. {analytics.values.DEPOSIT.toFixed(2)}</strong>
              </div>
              <div className="metric-block">
                <p>Withdraw Volume</p>
                <div className="bar-track">
                  <div className="bar-fill withdraw" style={{ width: `${(analytics.values.WITHDRAW / analytics.peak) * 100}%` }} />
                </div>
                <strong>Rs. {analytics.values.WITHDRAW.toFixed(2)}</strong>
              </div>
              <div className="metric-block">
                <p>Failed Volume</p>
                <div className="bar-track">
                  <div className="bar-fill failed" style={{ width: `${(analytics.values.FAILED / analytics.peak) * 100}%` }} />
                </div>
                <strong>Rs. {analytics.values.FAILED.toFixed(2)}</strong>
              </div>
            </div>

            <div className="table-wrap">
              <table className="table modern-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Date & Time</th>
                    <th>Status</th>
                    <th>Failure Reason</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTransactions.length === 0 ? (
                    <tr>
                      <td colSpan="6">No transactions found for selected filters.</td>
                    </tr>
                  ) : (
                    filteredTransactions.map((txn) => (
                      <tr key={txn.id}>
                        <td>{txn.id}</td>
                        <td>{txn.type}</td>
                        <td>Rs. {Number(txn.amount).toFixed(2)}</td>
                        <td>{new Date(txn.timestamp).toLocaleString()}</td>
                        <td>{txn.status || "SUCCESS"}</td>
                        <td>{txn.failureReason || "-"}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}
      </section>
    </AppLayout>
  );
}
