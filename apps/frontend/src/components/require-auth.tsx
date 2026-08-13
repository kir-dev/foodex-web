'use client';

import { PageState } from '@/components/page-state';
import { loginUrl, useAuth } from '@/components/auth-provider';
import { DetailedUserDto } from '@/types/api';
import { ReactNode } from 'react';

type RequireAuthProps = {
  children: ReactNode;
  allow?: (user: DetailedUserDto) => boolean;
  loadingLabel?: string;
};

export function RequireAuth({ children, allow, loadingLabel = 'Betöltés...' }: RequireAuthProps) {
  const { user, status } = useAuth();

  if (status === 'loading') {
    return <PageState>{loadingLabel}</PageState>;
  }

  if (status === 'unauthenticated' || !user) {
    return (
      <PageState>
        <a href={loginUrl} className='underline'>
          A folytatáshoz jelentkezz be
        </a>
      </PageState>
    );
  }

  if (allow && !allow(user)) {
    return <PageState>Nincs jogosultságod ehhez az oldalhoz.</PageState>;
  }

  return children;
}
