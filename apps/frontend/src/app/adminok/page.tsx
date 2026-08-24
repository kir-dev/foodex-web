'use client';

import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';
import { apiFetch, isApiError } from '@/lib/api';
import { AdminGrantDto, CreateAdminGrantDto, isSuperuser, UpdateAdminGrantDto } from '@/types/api';
import { useCallback, useEffect, useState } from 'react';

const AUTHSCH_PROFILE_URL = 'http://auth.sch.bme.hu/site/profile';

export default function AdminokPage() {
  return (
    <RequireAuth allow={isSuperuser} loadingLabel='Adminok betöltése...'>
      <AdminokContent />
    </RequireAuth>
  );
}

function AdminokContent() {
  const [grants, setGrants] = useState<AdminGrantDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [newName, setNewName] = useState('');
  const [newInternalId, setNewInternalId] = useState('');
  const [creating, setCreating] = useState(false);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');
  const [editingInternalId, setEditingInternalId] = useState('');
  const [saving, setSaving] = useState(false);

  const loadGrants = useCallback(async (): Promise<void> => {
    const data = await apiFetch<AdminGrantDto[]>('/api/admin-grants');
    setGrants(Array.isArray(data) ? data : []);
  }, []);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        await loadGrants();
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült betölteni az adminokat.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [loadGrants]);

  const handleCreate = async (): Promise<void> => {
    setMessage(null);
    const name = newName.trim();
    const internalId = newInternalId.trim();
    if (!name || !internalId) {
      setMessage({ text: 'Add meg a nevet és a belső azonosítót.', isError: true });
      return;
    }

    setCreating(true);
    try {
      const payload: CreateAdminGrantDto = { name, internalId };
      await apiFetch<AdminGrantDto>('/api/admin-grants', {
        method: 'POST',
        body: payload,
      });
      setNewName('');
      setNewInternalId('');
      await loadGrants();
      setMessage({ text: 'Admin hozzáadva.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült hozzáadni az admint.',
        isError: true,
      });
    } finally {
      setCreating(false);
    }
  };

  const handleSave = async (grantId: number): Promise<void> => {
    const name = editingName.trim();
    const internalId = editingInternalId.trim();
    if (!name || !internalId) {
      setMessage({ text: 'A név és a belső azonosító nem lehet üres.', isError: true });
      return;
    }

    setSaving(true);
    setMessage(null);
    try {
      const payload: UpdateAdminGrantDto = { name, internalId };
      await apiFetch<AdminGrantDto>(`/api/admin-grants/${grantId}`, {
        method: 'PUT',
        body: payload,
      });
      setEditingId(null);
      await loadGrants();
      setMessage({ text: 'Admin frissítve.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült frissíteni az admint.',
        isError: true,
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (grant: AdminGrantDto): Promise<void> => {
    if (!confirm(`Biztosan törlöd ${grant.name} admin jogosultságát?`)) {
      return;
    }

    setMessage(null);
    try {
      await apiFetch<void>(`/api/admin-grants/${grant.id}`, {
        method: 'DELETE',
        parseJson: false,
      });
      if (editingId === grant.id) {
        setEditingId(null);
      }
      await loadGrants();
      setMessage({ text: 'Admin törölve.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült törölni az admint.',
        isError: true,
      });
    }
  };

  if (loading) {
    return <PageState>Adminok betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>{error}</PageState>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center gap-6 bg-surface flex-1'>
      <div className='w-full max-w-5xl border-2 border-brand-fg rounded-2xl p-4 sm:p-6 space-y-4'>
        <h1 className='text-3xl font-bold text-brand-fg'>Adminok</h1>
        <p className='text-brand-fg'>
          <a
            href={AUTHSCH_PROFILE_URL}
            target='_blank'
            rel='noopener noreferrer'
            className='underline font-semibold text-accent'
          >
            Itt találod a belső azonosítódat:
          </a>
        </p>
        <p className='text-brand-fg'>
          Itt admin jogosultságot adhatsz felhasználóknak a belső azonosítójukkal. Szuperadmin jogosultságot ez nem ír
          felül.
        </p>

        <h2 className='text-2xl font-bold text-brand-fg'>Admin hozzáadása</h2>
        <div className='bg-brand text-white p-4 rounded-2xl border-2 border-accent flex flex-col sm:flex-row gap-4 items-end'>
          <div className='flex-1 w-full'>
            <StyledLabel>Név</StyledLabel>
            <StyledInput type='text' value={newName} onChange={(e) => setNewName(e.target.value)} maxLength={100} />
          </div>
          <div className='flex-1 w-full'>
            <StyledLabel>Belső azonosító</StyledLabel>
            <StyledInput
              type='text'
              value={newInternalId}
              onChange={(e) => setNewInternalId(e.target.value)}
              maxLength={36}
            />
          </div>
          <Button
            label={creating ? 'Hozzáadás...' : 'Admin hozzáadása'}
            variant='primary'
            onClick={() => void handleCreate()}
            disabled={creating}
          />
        </div>

        {message && (
          <p className={`text-lg font-medium ${message.isError ? 'text-red-500' : 'text-green-600'}`}>{message.text}</p>
        )}
      </div>

      <div className='w-full max-w-5xl border-2 border-brand-fg rounded-2xl p-4 sm:p-6 space-y-3'>
        <h2 className='text-2xl font-bold text-brand-fg'>Felvett adminok</h2>
        {grants.length === 0 ? (
          <p className='text-muted'>Még nincs admin a listán.</p>
        ) : (
          grants.map((grant) => (
            <div
              key={grant.id}
              className='border-2 border-brand-fg rounded-xl p-3 flex flex-col sm:flex-row sm:items-center gap-3'
            >
              <div className='flex-1 space-y-2'>
                {editingId === grant.id ? (
                  <>
                    <StyledInput
                      type='text'
                      value={editingName}
                      onChange={(e) => setEditingName(e.target.value)}
                      maxLength={100}
                      className='!mt-0 border-2 border-input-border'
                    />
                    <StyledInput
                      type='text'
                      value={editingInternalId}
                      onChange={(e) => setEditingInternalId(e.target.value)}
                      maxLength={36}
                      className='!mt-0 border-2 border-input-border'
                    />
                  </>
                ) : (
                  <>
                    <p className='text-xl font-semibold text-brand-fg'>{grant.name}</p>
                    <p className='text-sm text-muted break-all'>{grant.internalId}</p>
                  </>
                )}
              </div>
              <div className='flex flex-wrap gap-2'>
                {editingId === grant.id ? (
                  <>
                    <Button
                      label={saving ? 'Mentés...' : 'Mentés'}
                      variant='primary'
                      onClick={() => void handleSave(grant.id)}
                      disabled={saving}
                    />
                    <Button label='Mégse' variant='secondary' onClick={() => setEditingId(null)} disabled={saving} />
                  </>
                ) : (
                  <>
                    <Button
                      label='Szerkesztés'
                      variant='secondary'
                      onClick={() => {
                        setEditingId(grant.id);
                        setEditingName(grant.name);
                        setEditingInternalId(grant.internalId);
                      }}
                    />
                    <Button label='Törlés' variant='secondary' onClick={() => void handleDelete(grant)} />
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
