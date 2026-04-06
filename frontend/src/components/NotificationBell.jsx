import { Bell } from "lucide-react";

export default function NotificationBell({ count = 0, onClick }) {
  return (
    <button className="notification-bell" onClick={onClick} type="button" aria-label="alerts">
      <Bell size={20} />
      {count > 0 ? <span className="badge-count">{count}</span> : null}
    </button>
  );
}
