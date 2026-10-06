import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, LoginPayload, RegisterPayload } from '../types';
import { api, authStorage } from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => authStorage.getUser());
  const [token, setToken] = useState<string | null>(() => authStorage.getToken());
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    async function verifyUser() {
      const storedToken = authStorage.getToken();
      if (storedToken) {
        const verifiedUser = await api.getCurrentUser();
        if (verifiedUser) {
          setUser(verifiedUser);
        } else {
          // Token expired or invalid
          setUser(null);
          setToken(null);
        }
      }
      setIsLoading(false);
    }

    verifyUser();
  }, []);

  const login = async (payload: LoginPayload) => {
    const response = await api.login(payload);
    setUser(response.user);
    setToken(response.token);
  };

  const register = async (payload: RegisterPayload) => {
    const response = await api.register(payload);
    setUser(response.user);
    setToken(response.token);
  };

  const logout = () => {
    authStorage.clearAuth();
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
