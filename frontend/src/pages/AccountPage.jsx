import { useEffect, useState } from "react";
import { toast } from "react-hot-toast";
import AppLayout from "../components/AppLayout";
import Loader from "../components/Loader";
import { accountApi, emailApi } from "../api/bankApi";
import { useAuth } from "../context/AuthContext";

export default function AccountPage() {
  const { auth } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [isSendingEmail, setIsSendingEmail] = useState(false);
  const [createForm, setCreateForm] = useState({ accountType: "SAVINGS", initialDeposit: 0 });

  const loadAccounts = async () => {
    setLoading(true);
    try {
      const { data } = await accountApi.getAccountsByUser(auth.userId);
      setAccounts(data || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, [auth.userId]);

  const handleCreate = async (event) => {
    event.preventDefault();
    if (Number(createForm.initialDeposit) < 0) {
      toast.error("Initial deposit cannot be negative");
      return;
    }

    setIsCreating(true);
    try {
      await accountApi.createAccount({
        userId: auth.userId,
        accountType: createForm.accountType,
        initialDeposit: Number(createForm.initialDeposit)
      });
      toast.success("Account created successfully");
      await loadAccounts();

      setIsSendingEmail(true);
      try {
        await emailApi.send({
          userId: auth.userId,
          email: auth.email,
          eventType: "ACCOUNT_CREATED",
          content: "Welcome to INBANK. Your account has been created."
        });
        toast.success("Welcome email sent");
      } catch {
        toast.error("Email failed to send");
      } finally {
        setIsSendingEmail(false);
      }
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <AppLayout>
      <section className="glass panel">
        <h2>Account Management</h2>
        <form className="row g-3" onSubmit={handleCreate}>
          <div className="col-md-4">
            <label className="form-label">Account Type</label>
            <select
              className="form-select"
              value={createForm.accountType}
              onChange={(event) => setCreateForm((prev) => ({ ...prev, accountType: event.target.value }))}
            >
              <option value="SAVINGS">Savings</option>
              <option value="CURRENT">Current</option>
            </select>
          </div>
          <div className="col-md-4">
            <label className="form-label">Initial Deposit</label>
            <input
              className="form-control"
              type="number"
              min="0"
              value={createForm.initialDeposit}
              onChange={(event) => setCreateForm((prev) => ({ ...prev, initialDeposit: event.target.value }))}
            />
          </div>
          <div className="col-md-4 align-self-end">
            <button className="btn btn-primary w-100" type="submit" disabled={isCreating || isSendingEmail}>
              {isCreating ? "Creating..." : "Create Account"}
            </button>
          </div>
        </form>
        {isSendingEmail ? <Loader text="Sending welcome email..." /> : null}
      </section>

      <section className="glass panel mt-4">
        <h3>Your Accounts</h3>
        {loading ? (
          <Loader text="Loading accounts..." />
        ) : (
          <div className="table-wrap">
            <table className="table modern-table">
              <thead>
                <tr>
                  <th>Account Number</th>
                  <th>Account Type</th>
                  <th>Current Balance</th>
                </tr>
              </thead>
              <tbody>
                {accounts.length === 0 ? (
                  <tr>
                    <td colSpan="3">No accounts yet. Create your first account above.</td>
                  </tr>
                ) : (
                  accounts.map((account) => (
                    <tr key={account.id}>
                      <td>{account.id}</td>
                      <td>{account.accountType}</td>
                      <td>Rs. {Number(account.balance).toFixed(2)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </AppLayout>
  );
}
