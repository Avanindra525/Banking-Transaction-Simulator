import { useEffect, useMemo, useState } from "react";
import { toast } from "react-hot-toast";
import AppLayout from "../components/AppLayout";
import Loader from "../components/Loader";
import { accountApi, reportApi } from "../api/bankApi";
import { useAuth } from "../context/AuthContext";

export default function ReportsPage() {
  const { auth } = useAuth();
  const [loading, setLoading] = useState(true);
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [totals, setTotals] = useState({ totalDeposits: 0, totalWithdrawals: 0, totalFailed: 0 });
  const [filters, setFilters] = useState({ type: "ALL", fromDate: "", toDate: "" });

  const loadData = async (activeFilters = filters) => {
    setLoading(true);
    try {
      const [accountRes, reportRes] = await Promise.all([
        accountApi.getAccountsByUser(auth.userId),
        reportApi.getReport({ userId: auth.userId, ...activeFilters })
      ]);
      setAccounts(accountRes.data || []);
      setTransactions(reportRes.data?.transactions || []);
      setTotals({
        totalDeposits: Number(reportRes.data?.totalDeposits || 0),
        totalWithdrawals: Number(reportRes.data?.totalWithdrawals || 0),
        totalFailed: Number(reportRes.data?.totalFailed || 0)
      });
    } catch {
      toast.error("Failed to load report data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(filters);
  }, [auth.userId, filters]);

  const filteredTransactions = useMemo(() => transactions, [transactions]);

  const totalBalance = useMemo(() => accounts.reduce((sum, item) => sum + Number(item.balance || 0), 0), [accounts]);

  const exportCsv = async () => {
    if (transactions.length === 0) {
      toast.error("No records to export");
      return;
    }
    try {
      const { data } = await reportApi.downloadCsv({ userId: auth.userId, ...filters });
      const blob = new Blob([data], { type: "text/csv;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `inbank-report-${Date.now()}.csv`;
      link.click();
      URL.revokeObjectURL(url);
    } catch {
      toast.error("Unable to export CSV right now");
    }
  };

  return (
    <AppLayout>
      <section className="glass panel">
        <h2>Reporting Hub</h2>
        {loading ? (
          <Loader text="Building report..." />
        ) : (
          <>
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

            <div className="summary-row mt-3">
              <div className="summary-chip">Total Balance: Rs. {totalBalance.toFixed(2)}</div>
              <div className="summary-chip">Filtered Transactions: {filteredTransactions.length}</div>
              <div className="summary-chip">Deposits: Rs. {totals.totalDeposits.toFixed(2)}</div>
              <div className="summary-chip">Withdrawals: Rs. {totals.totalWithdrawals.toFixed(2)}</div>
              <div className="summary-chip">Failed: Rs. {totals.totalFailed.toFixed(2)}</div>
            </div>

            <div className="table-wrap mt-3">
              <table className="table modern-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Date & Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTransactions.length === 0 ? (
                    <tr>
                      <td colSpan="5">No transactions found for selected filters.</td>
                    </tr>
                  ) : (
                    filteredTransactions.map((txn) => (
                      <tr key={txn.id}>
                        <td>{txn.id}</td>
                        <td>{txn.type}</td>
                        <td>Rs. {Number(txn.amount).toFixed(2)}</td>
                        <td>{new Date(txn.timestamp).toLocaleString()}</td>
                        <td>{txn.status || "SUCCESS"}</td>
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
