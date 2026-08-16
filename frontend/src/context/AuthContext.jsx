import React, { createContext, useContext, useState, useEffect } from 'react';
import { loginApi, logoutApi, getAuthToken, setAuthToken } from '../services/api';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = getAuthToken();
    const storedUser = localStorage.getItem('pos_user');
    if (token && storedUser) {
      try {
        return JSON.parse(storedUser);
      } catch (e) {
        return null;
      }
    }
    return null;
  });

  const [loading, setLoading] = useState(false);

  const login = async (username, password) => {
    setLoading(true);
    try {
      const data = await loginApi(username, password);
      const userData = {
        username: data.username || username,
        role: data.role || 'ADMINISTRATOR',
        fullName: data.fullName || (username === 'admin' ? 'Administrador Principal' : username),
        token: data.token
      };
      setAuthToken(data.token);
      localStorage.setItem('pos_user', JSON.stringify(userData));
      setUser(userData);
      setLoading(false);
      return userData;
    } catch (err) {
      setLoading(false);
      throw err;
    }
  };

  const logout = async () => {
    setLoading(true);
    await logoutApi();
    localStorage.removeItem('pos_user');
    setUser(null);
    setLoading(false);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
