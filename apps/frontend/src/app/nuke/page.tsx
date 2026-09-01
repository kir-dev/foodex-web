'use client';

import Button from '@/components/button';
import { RequireAuth } from '@/components/require-auth';
import { apiFetch, isApiError } from '@/lib/api';
import { isSuperuser } from '@/types/api';
import { useState } from 'react';

export default function NukePage() {
  return (
    <RequireAuth allow={isSuperuser} loadingLabel='Nuke betöltése...'>
      <NukeContent />
    </RequireAuth>
  );
}

function NukeContent() {
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [message, setMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const handleDelete = async (): Promise<void> => {
    setDeleting(true);
    setMessage(null);
    try {
      await apiFetch<void>('/api/nuke', {
        method: 'DELETE',
        parseJson: false,
      });
      setConfirmOpen(false);
      setMessage({ text: 'Az összes nyitás kérelem, nyitás és műszak törölve.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült törölni az adatokat.',
        isError: true,
      });
    } finally {
      setDeleting(false);
    }
  };

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center gap-6 bg-surface flex-1'>
      <div className='w-full max-w-5xl border-2 border-brand-fg rounded-2xl p-4 sm:p-6 space-y-4'>
        <h1 className='text-3xl font-bold text-brand-fg'>Nuke💣</h1>
        <p className='text-brand-fg'>
          Szeretnéd törölni az összes nyitás kérelmet, nyitást és műszakot az adatbázisból? Ez a művelet nem
          visszacsinálható!
        </p>
        <Button
          label='Végleges törlés'
          variant='danger'
          onClick={() => {
            setMessage(null);
            setConfirmOpen(true);
          }}
          disabled={deleting}
        />
        {message && (
          <p className={`text-lg font-medium ${message.isError ? 'text-red-500' : 'text-green-600'}`}>{message.text}</p>
        )}
      </div>

      {confirmOpen && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
          <div className='bg-surface rounded-xl border-2 border-brand-fg p-6 max-w-lg w-full space-y-4 shadow-xl'>
            <p className='text-2xl font-bold text-brand-fg'>Biztosan véglegesen törlöd?</p>
            {message?.isError && <p className='text-lg font-medium text-red-500'>{message.text}</p>}
            <div className='flex justify-end gap-2 pt-2'>
              <Button
                label='Mégse'
                variant='secondary'
                onClick={() => setConfirmOpen(false)}
                disabled={deleting}
              />
              <Button
                label={deleting ? 'Törlés...' : 'Törlés'}
                variant='danger'
                onClick={() => void handleDelete()}
                disabled={deleting}
              />
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
