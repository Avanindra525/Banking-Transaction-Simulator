import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { accountApi, alertApi } from "../api/bankApi";
import { useAuth } from "../context/AuthContext";
import Sidebar from "./Sidebar";
import NotificationBell from "./NotificationBell";

const ALERT_READ_KEY = "inbank-alert-last-seen";

export default function AppLayout({ children }) {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    let timer;
    let active = true;

    const loadAlerts = async () => {
      try {
        const { data: accountData } = await accountApi.getAccountsByUser(auth.userId);
        const accountList = accountData || [];
        if (accountList.length === 0) {
          if (active) {
            setAlerts([]);
          }
          return;
        }

        const responses = await Promise.all(accountList.map((acc) => alertApi.getAlertsByAccountId(acc.id, auth.userId)));
        const merged = responses
          .flatMap((item) => item.data || [])
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        if (active) {
          setAlerts(merged);
        }
      } catch {
        if (active) {
          setAlerts([]);
        }
      }
    };

    loadAlerts();
    timer = setInterval(loadAlerts, 20000);
    return () => {
      active = false;
      if (timer) {
        clearInterval(timer);
      }
    };
  }, [auth.userId]);

  const unreadCount = useMemo(() => {
    const lastSeen = Number(localStorage.getItem(ALERT_READ_KEY) || 0);
    return alerts.filter((item) => new Date(item.createdAt).getTime() > lastSeen).length;
  }, [alerts]);

  const handleBellClick = () => {
    localStorage.setItem(ALERT_READ_KEY, String(Date.now()));
    navigate("/alerts");
  };

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main-shell">
        <header className="main-header glass">
          <p className="section-title">INBANK - Smart Banking System</p>
          <NotificationBell count={unreadCount} onClick={handleBellClick} />
        </header>
        <main className="page-wrap">{children}</main>
      </div>
    </div>
  );
}
