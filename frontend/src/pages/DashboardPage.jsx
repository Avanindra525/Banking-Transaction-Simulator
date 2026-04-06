import { useEffect, useState } from "react";
import { toast } from "react-hot-toast";
import AppLayout from "../components/AppLayout";
import Loader from "../components/Loader";
import { accountApi, transactionApi } from "../api/bankApi";
import { useAuth } from "../context/AuthContext";

export default function DashboardPage() {
  const { auth } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboard = async () => {
      setLoading(true);
      try {
        const [accountsRes, transactionsRes] = await Promise.all([
          accountApi.getAccountsByUser(auth.userId),
          transactionApi.historyByUser(auth.userId)
        ]);
        setAccounts(accountsRes.data || []);
        setTransactions(transactionsRes.data || []);
      } catch {
        toast.error("Unable to fetch dashboard data");
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, [auth.userId]);

  if (loading) {
    return (
      <AppLayout>
        <Loader text="Loading your dashboard..." />
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <section className="glass panel">
        <h2>Welcome, {auth.fullName}</h2>
        <p>{auth.email}</p>
      </section>

      <section className="card-grid">
        {accounts.length === 0 ? (
          <article className="glass panel">No account found. Create one from Account tab.</article>
        ) : (
          accounts.map((account) => (
            <article key={account.id} className="glass panel">
              <h3>Account #{account.id}</h3>
              <p>Type: {account.accountType}</p>
              <h4>Balance: Rs. {Number(account.balance).toFixed(2)}</h4>
            </article>
          ))
        )}
      </section>

      <section className="glass panel">
        <h3>Recent Transactions</h3>
        <div className="table-wrap">
          <table className="table modern-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {transactions.slice(0, 5).map((txn) => (
                <tr key={txn.id}>
                  <td>{txn.id}</td>
                  <td>{txn.type}</td>
                  <td>Rs. {Number(txn.amount).toFixed(2)}</td>
                  <td>{new Date(txn.timestamp).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </AppLayout>
  );
}
