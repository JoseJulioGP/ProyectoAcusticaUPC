import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('acu-token'));
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('acu-user')); } catch { return null; }
  });

  const setSession = (res) => {
    const u = res.user ?? res;
    setUser(u);
    localStorage.setItem('acu-user', JSON.stringify(u));
    setToken(localStorage.getItem('acu-token'));
  };
  const logout = () => {
    localStorage.removeItem('acu-token');
    localStorage.removeItem('acu-user');
    setToken(null); setUser(null);
  };

  return (
    <AuthContext.Provider value={{ token, user, role: user?.role ?? 'VIEWER', setSession, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);