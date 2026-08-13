'use client';

import { apiFetch, isApiError } from '@/lib/api';
import { loginUrl, logoutUrl } from '@/lib/config';
import { DetailedUserDto, isClubLeaderOrAdmin } from '@/types/api';
import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useState } from 'react';

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
  user: DetailedUserDto | null;
  status: AuthStatus;
  canManageRequests: boolean;
  refresh: () => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<DetailedUserDto | null>(null);
  const [status, setStatus] = useState<AuthStatus>('loading');

  const refresh = useCallback(async () => {
    try {
      const me = await apiFetch<DetailedUserDto>('/api/users/me');
      setUser(me);
      setStatus('authenticated');
    } catch (error) {
      setUser(null);
      if (isApiError(error) && error.status === 401) {
        setStatus('unauthenticated');
        return;
      }
      setStatus('unauthenticated');
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiFetch<void>(logoutUrl, {
        method: 'POST',
        parseJson: false,
        redirect: 'manual',
      });
    } catch (error) {
      if (!isApiError(error) || error.status !== 401) {
        console.error(error);
      }
    } finally {
      setUser(null);
      setStatus('unauthenticated');
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      status,
      canManageRequests: user ? isClubLeaderOrAdmin(user) : false,
      refresh,
      logout,
    }),
    [user, status, refresh, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

export { loginUrl };
