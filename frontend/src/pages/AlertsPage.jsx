import { useEffect, useState } from "react";
import { toast } from "react-hot-toast";
import AppLayout from "../components/AppLayout";
import Loader from "../components/Loader";
import { accountApi, alertApi } from "../api/bankApi";
import { useAuth } from "../context/AuthContext";

export default function AlertsPage() {
  const { auth } = useAuth();
  const [alerts, setAlerts] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let intervalId;
    let active = true;

    const loadAlerts = async () => {
      setLoading(true);
      try {
        const { data: accountData } = await accountApi.getAccountsByUser(auth.userId);
        const accountList = accountData || [];
        if (active) {
          setAccounts(accountList);
        }

        const { data: userAlerts } = await alertApi.getAlertsByUser(auth.userId);

        const mergedAlerts = (userAlerts || [])
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        if (active) {
          setAlerts(mergedAlerts);
        }
      } catch {
        if (active) {
          toast.error("Failed to load alerts");
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadAlerts();
    intervalId = setInterval(loadAlerts, 20000);

    return () => {
      active = false;
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [auth.userId]);

  return (
    <AppLayout>
      <section className="glass panel">
        <h2>Alerts & Notifications</h2>
        <p>Monitoring {accounts.length} account(s) for low balance and large transactions.</p>
        {loading ? (
          <Loader text="Loading alerts..." />
        ) : alerts.length === 0 ? (
          <p>No alerts right now.</p>
        ) : (
          <div className="alert-grid">
            {alerts.map((alert, index) => (
              <article key={`${alert.accountId}-${index}`} className="alert-card">
                <h4>{alert.title}</h4>
                <p>{alert.message}</p>
                <small>{new Date(alert.createdAt).toLocaleString()}</small>
              </article>
            ))}
          </div>
        )}
      </section>
    </AppLayout>
  );
}
