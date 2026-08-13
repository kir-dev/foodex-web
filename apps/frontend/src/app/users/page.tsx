'use client';

import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { StyledInput } from '@/components/styledInput';
import { apiFetch, isApiError } from '@/lib/api';
import { DetailedUserDto, isAdmin } from '@/types/api';
import { useEffect, useMemo, useState } from 'react';

export default function UsersPage() {
  return (
    <RequireAuth allow={isAdmin} loadingLabel='Felhasználók betöltése...'>
      <UsersContent />
    </RequireAuth>
  );
}

function UsersContent() {
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
    return users.filter(
      (user) =>
        user.name.toLowerCase().includes(q) ||
        (user.nickname ?? '').toLowerCase().includes(q) ||
        user.email.toLowerCase().includes(q) ||
        user.role.toLowerCase().includes(q)
    );
  }, [query, users]);

  if (loading) {
    return <PageState>Felhasználók betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>{error}</PageState>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center bg-white min-h-screen'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6 space-y-4'>
        <h1 className='text-3xl font-bold text-[#332C81]'>Aktív felhasználók</h1>
        <StyledInput
          type='search'
          placeholder='Keresés név, becenév, email vagy szerep szerint...'
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />

        <div className='flex flex-col gap-3'>
          {filtered.length === 0 ? (
            <p className='text-gray-500'>Nincs találat.</p>
          ) : (
            filtered.map((user) => (
              <div key={user.id} className='border-2 border-[#332C81] rounded-xl p-3'>
                <div className='flex flex-col sm:flex-row sm:justify-between gap-1'>
                  <p className='text-xl font-semibold text-[#332C81]'>
                    {user.name} <span className='font-normal text-gray-600'>({user.nickname})</span>
                  </p>
                  <span className='text-sm font-bold text-[#FF9860]'>{user.role}</span>
                </div>
                <p className='text-gray-600'>{user.email}</p>
                {user.leaderAt.length > 0 && (
                  <p className='text-sm text-gray-500'>Vezető: {user.leaderAt.map((club) => club.name).join(', ')}</p>
                )}
                <p className='text-sm text-gray-500'>Műszakok: {user.shifts.length}</p>
              </div>
            ))
          )}
        </div>
      </div>
    </main>
  );
}
