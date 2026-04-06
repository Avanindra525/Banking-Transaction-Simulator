import { createContext, useContext, useMemo, useState } from "react";

const AUTH_KEY = "inbank-auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const raw = localStorage.getItem(AUTH_KEY);
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw);
    if (parsed?.expiresAt && Date.now() > parsed.expiresAt) {
      localStorage.removeItem(AUTH_KEY);
      return null;
    }
    return parsed;
  });

  const login = (payload) => {
    const session = {
      userId: payload.userId,
      fullName: payload.fullName,
      email: payload.email,
      token: payload.token,
      expiresAt: payload.expiresAt,
      isLoggedIn: true
    };
    setAuth(session);
    localStorage.setItem(AUTH_KEY, JSON.stringify(session));
  };

  const logout = () => {
    setAuth(null);
    localStorage.removeItem(AUTH_KEY);
  };

  const value = useMemo(
    () => ({
      auth,
      isAuthenticated: Boolean(auth?.isLoggedIn && auth?.token && (!auth?.expiresAt || Date.now() <= auth.expiresAt)),
      login,
      logout
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
