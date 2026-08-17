'use client';

import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';
import { apiFetch, isApiError } from '@/lib/api';
import { CreateCookingClubDto, DetailedCookingClubDto, isAdmin, UpdateCookingClubDto } from '@/types/api';
import { useCallback, useEffect, useState } from 'react';

export default function ClubsPage() {
  return (
    <RequireAuth allow={isAdmin} loadingLabel='Körök betöltése...'>
      <ClubsContent />
    </RequireAuth>
  );
}

function ClubsContent() {
  const [clubs, setClubs] = useState<DetailedCookingClubDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [newId, setNewId] = useState('');
  const [newName, setNewName] = useState('');
  const [creating, setCreating] = useState(false);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [saving, setSaving] = useState(false);

  const loadClubs = useCallback(async (): Promise<void> => {
    const data = await apiFetch<DetailedCookingClubDto[]>('/api/cooking-clubs');
    setClubs(Array.isArray(data) ? data : []);
  }, []);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        await loadClubs();
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a köröket.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [loadClubs]);

  const handleCreate = async (): Promise<void> => {
    setMessage(null);
    const id = Number(newId);
    if (!Number.isInteger(id) || id <= 0 || !newName.trim()) {
      setMessage({ text: 'Adj meg egy pozitív AuthSCH kör ID-t és egy nevet.', isError: true });
      return;
    }

    setCreating(true);
    try {
      const payload: CreateCookingClubDto = { id, name: newName.trim() };
      await apiFetch<DetailedCookingClubDto>('/api/cooking-clubs', {
        method: 'POST',
        body: payload,
      });
      setNewId('');
      setNewName('');
      await loadClubs();
      setMessage({ text: 'Kör létrehozva.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült létrehozni a kört.',
        isError: true,
      });
    } finally {
      setCreating(false);
    }
  };

  const handleSaveName = async (clubId: number): Promise<void> => {
    if (!editingName.trim()) {
      setMessage({ text: 'A kör neve nem lehet üres.', isError: true });
      return;
    }

    setSaving(true);
    setMessage(null);
    try {
      const payload: UpdateCookingClubDto = { id: clubId, name: editingName.trim() };
      await apiFetch<DetailedCookingClubDto>(`/api/cooking-clubs/${clubId}`, {
        method: 'PUT',
        body: payload,
      });
      setEditingId(null);
      await loadClubs();
      setMessage({ text: 'Kör neve frissítve.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült frissíteni a kört.',
        isError: true,
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (club: DetailedCookingClubDto): Promise<void> => {
    if (!confirm(`Biztosan törlöd a(z) ${club.name} kört?`)) {
      return;
    }

    setMessage(null);
    try {
      await apiFetch<void>(`/api/cooking-clubs/${club.id}`, {
        method: 'DELETE',
        parseJson: false,
      });
      await loadClubs();
      setMessage({ text: 'Kör törölve.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült törölni a kört.',
        isError: true,
      });
    }
  };

  if (loading) {
    return <PageState>Körök betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>{error}</PageState>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center gap-6 bg-white flex-1'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6 space-y-4'>
        <h1 className='text-3xl font-bold text-[#332C81]'>Kajás körök</h1>
        <p className='text-[#332C81]'>
          Új kör felvételekor az ID az AuthSCH körazonosító (például Pizzásch 223). A vezetőket a belépéskor a backend
          állítja be.
        </p>

        <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860] flex flex-col sm:flex-row gap-4 items-end'>
          <div className='w-full sm:w-40'>
            <StyledLabel>Kör ID</StyledLabel>
            <StyledInput type='number' min={1} value={newId} onChange={(e) => setNewId(e.target.value)} />
          </div>
          <div className='flex-1 w-full'>
            <StyledLabel>Név</StyledLabel>
            <StyledInput type='text' value={newName} onChange={(e) => setNewName(e.target.value)} />
          </div>
          <Button
            label={creating ? 'Létrehozás...' : 'Kör hozzáadása'}
            variant='primary'
            onClick={() => void handleCreate()}
            disabled={creating}
          />
        </div>

        {message && (
          <p className={`text-lg font-medium ${message.isError ? 'text-red-500' : 'text-green-600'}`}>{message.text}</p>
        )}
      </div>

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6 space-y-3'>
        <h2 className='text-2xl font-bold text-[#332C81]'>Meglévő körök</h2>
        {clubs.length === 0 ? (
          <p className='text-gray-500'>Még nincs kör a rendszerben.</p>
        ) : (
          clubs.map((club) => (
            <div
              key={club.id}
              className='border-2 border-[#332C81] rounded-xl p-3 flex flex-col sm:flex-row sm:items-center gap-3'
            >
              <div className='flex-1'>
                <p className='text-sm text-gray-500'>ID: {club.id}</p>
                {editingId === club.id ? (
                  <input
                    type='text'
                    className='border-2 border-gray-300 rounded-lg p-2 text-black w-full max-w-md'
                    value={editingName}
                    onChange={(e) => setEditingName(e.target.value)}
                  />
                ) : (
                  <p className='text-xl font-semibold text-[#332C81]'>{club.name}</p>
                )}
                <p className='text-sm text-gray-600'>
                  Vezetők: {club.leaders.length > 0 ? club.leaders.map((leader) => leader.nickname).join(', ') : '—'}
                </p>
              </div>
              <div className='flex flex-wrap gap-2'>
                {editingId === club.id ? (
                  <>
                    <Button
                      label={saving ? 'Mentés...' : 'Mentés'}
                      variant='primary'
                      onClick={() => void handleSaveName(club.id)}
                      disabled={saving}
                    />
                    <Button label='Mégse' variant='secondary' onClick={() => setEditingId(null)} disabled={saving} />
                  </>
                ) : (
                  <>
                    <Button
                      label='Átnevezés'
                      variant='secondary'
                      onClick={() => {
                        setEditingId(club.id);
                        setEditingName(club.name);
                      }}
                    />
                    <Button label='Törlés' variant='secondary' onClick={() => void handleDelete(club)} />
                  </>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </main>
  );
}
