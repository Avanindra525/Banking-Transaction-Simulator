import { Link, NavLink } from "react-router-dom";
import { LayoutDashboard, WalletCards, ReceiptText, BellRing, BarChart3, LogOut } from "lucide-react";
import { useAuth } from "../context/AuthContext";

export default function Sidebar() {
  const { logout } = useAuth();

  return (
    <aside className="side-panel">
      <Link to="/dashboard" className="brand-block">
        <h1>INBANK</h1>
        <p>Smart Banking System</p>
      </Link>

      <nav className="nav-stack">
        <NavLink to="/dashboard" className="nav-item">
          <LayoutDashboard size={18} /> Dashboard
        </NavLink>
        <NavLink to="/account" className="nav-item">
          <WalletCards size={18} /> Account
        </NavLink>
        <NavLink to="/transactions" className="nav-item">
          <ReceiptText size={18} /> Transactions
        </NavLink>
        <NavLink to="/alerts" className="nav-item">
          <BellRing size={18} /> Alerts
        </NavLink>
        <NavLink to="/reports" className="nav-item">
          <BarChart3 size={18} /> Reports
        </NavLink>
      </nav>

      <button type="button" className="logout-btn" onClick={logout}>
        <LogOut size={18} /> Logout
      </button>
    </aside>
  );
}
