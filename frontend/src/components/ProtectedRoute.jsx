import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, logout } = useAuth();

  if (!isAuthenticated) {
    logout();
    return <Navigate to="/login" replace />;
  }

  return children;
}
