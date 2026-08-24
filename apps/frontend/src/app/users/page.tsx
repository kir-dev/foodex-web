'use client';

import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { StyledInput } from '@/components/styledInput';
import { UserNameLink } from '@/components/userNameLink';
import { apiFetch, isApiError } from '@/lib/api';
import { useAuth } from '@/components/auth-provider';
import { DetailedUserDto, ROLE_LABEL } from '@/types/api';
import { useEffect, useMemo, useState } from 'react';

export default function UsersPage() {
  return (
    <RequireAuth loadingLabel='Felhasználók betöltése...'>
      <UsersContent />
    </RequireAuth>
  );
}

function UsersContent() {
  const { isAdminUser } = useAuth();
  const [users, setUsers] = useState<DetailedUserDto[]>([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        const data = await apiFetch<DetailedUserDto[]>('/api/users');
        setUsers(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a felhasználókat.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) {
      return users;
    }
    return users.filter((user) => {
      const roleLabel = ROLE_LABEL[user.role] ?? user.role;
      return (
        user.name.toLowerCase().includes(q) ||
        (user.nickname ?? '').toLowerCase().includes(q) ||
        (isAdminUser && user.email.toLowerCase().includes(q)) ||
        user.role.toLowerCase().includes(q) ||
        roleLabel.toLowerCase().includes(q)
      );
    });
  }, [query, users, isAdminUser]);

  if (loading) {
    return <PageState>Felhasználók betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>{error}</PageState>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center bg-surface flex-1'>
      <div className='w-full max-w-5xl border-2 border-brand-fg rounded-2xl p-4 sm:p-6 space-y-4'>
        <h1 className='text-3xl font-bold text-brand-fg'>Aktív felhasználók</h1>
        <StyledInput
          type='search'
          placeholder={
            isAdminUser
              ? 'Keresés név, becenév, email vagy szerep szerint...'
              : 'Keresés név, becenév vagy szerep szerint...'
          }
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className='border-2 border-brand-fg'
        />

        <div className='flex flex-col gap-3'>
          {filtered.length === 0 ? (
            <p className='text-muted'>Nincs találat.</p>
          ) : (
            filtered.map((user) => (
              <div key={user.id} className='border-2 border-brand-fg rounded-xl p-3'>
                <div className='flex flex-col sm:flex-row sm:justify-between gap-1'>
                  <p className='text-xl font-semibold text-brand-fg'>
                    <UserNameLink userId={user.id}>{user.name}</UserNameLink>{' '}
                    <span className='font-normal text-muted'>
                      (<UserNameLink userId={user.id}>{user.nickname}</UserNameLink>)
                    </span>
                  </p>
                  <span className='text-sm font-bold text-accent'>{ROLE_LABEL[user.role] ?? user.role}</span>
                </div>
                {isAdminUser && <p className='text-muted'>{user.email}</p>}
                {user.leaderAt.length > 0 && (
                  <p className='text-sm text-muted'>Vezető: {user.leaderAt.map((club) => club.name).join(', ')}</p>
                )}
                <p className='text-sm text-muted'>Műszakok: {user.shifts.length}</p>
              </div>
            ))
          )}
        </div>
      </div>
    </main>
  );
}
