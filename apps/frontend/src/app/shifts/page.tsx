'use client';

import { ActiveShiftsContainer } from '@/components/activeShiftsContainer';
import { useAuth } from '@/components/auth-provider';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { Shift } from '@/components/ShiftTable';
import { SubmitShiftsContainer } from '@/components/submitShiftsContainer';
import { apiFetch, isApiError, shiftActionErrorMessage } from '@/lib/api';
import { shiftToRow } from '@/lib/shift-view';
import { useRefetchOnPath } from '@/lib/use-refetch-on-path';
import { ActiveAndFullShifts, canJoinShifts, DetailedShiftDto } from '@/types/api';
import { useCallback, useState } from 'react';

export default function ShiftsPage() {
  return (
    <RequireAuth loadingLabel='Műszakok betöltése...'>
      <ShiftsContent />
    </RequireAuth>
  );
}

function ShiftsContent() {
  const { user, refresh } = useAuth();
  const [data, setData] = useState<ActiveAndFullShifts | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const loadShifts = useCallback(async (): Promise<void> => {
    const shiftsData = await apiFetch<ActiveAndFullShifts>('/api/shifts');
    setData({
      activeShifts: Array.isArray(shiftsData.activeShifts) ? shiftsData.activeShifts : [],
      fullShifts: Array.isArray(shiftsData.fullShifts) ? shiftsData.fullShifts : [],
    });
  }, []);

  useRefetchOnPath(async () => {
    try {
      await loadShifts();
    } catch (err) {
      setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a műszakok adatait.');
    } finally {
      setLoading(false);
    }
  });

  if (loading) {
    return <PageState>Műszakok betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba történt: {error}</PageState>;
  }

  if (!data || !user) {
    return null;
  }

  const allowJoin = canJoinShifts(user);
  const toRow = (shift: DetailedShiftDto): Shift => shiftToRow(shift, user);

  const handleJoin = async (shiftRow: Shift): Promise<void> => {
    if (!shiftRow.canJoin) {
      return;
    }
    setActionMessage(null);
    try {
      await apiFetch<DetailedShiftDto>(`/api/shifts/${shiftRow.id}/${user.id}`, { method: 'POST' });
      await loadShifts();
      await refresh();
      setActionMessage({ text: 'Sikeres jelentkezés.', isError: false });
    } catch (err) {
      await loadShifts();
      setActionMessage({
        text: shiftActionErrorMessage(err, 'join'),
        isError: true,
      });
    }
  };

  const handleLeave = async (shiftRow: Shift): Promise<void> => {
    if (!shiftRow.canLeave) {
      return;
    }

    setActionMessage(null);
    try {
      await apiFetch<DetailedShiftDto>(`/api/shifts/${shiftRow.id}/${user.id}`, { method: 'DELETE' });
      await loadShifts();
      await refresh();
      setActionMessage({ text: 'Műszak leadva.', isError: false });
    } catch (err) {
      await loadShifts();
      setActionMessage({
        text: shiftActionErrorMessage(err, 'leave'),
        isError: true,
      });
    }
  };

  return (
    <main className='p-6 flex flex-col items-center gap-6 bg-white flex-1'>
      {actionMessage && (
        <p
          className={`w-full max-w-5xl text-lg font-medium ${actionMessage.isError ? 'text-red-500' : 'text-green-600'}`}
        >
          {actionMessage.text}
        </p>
      )}

      {user.role === 'NEWBIE' && (
        <p className='w-full max-w-5xl text-[#332C81]'>
          Újoncként akkor tudsz jelentkezni, ha már van legalább egy tag a műszakban, és kevesebb újonc van, mint tag.
        </p>
      )}

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Aktív műszakok</h3>
        <ActiveShiftsContainer
          shifts={data.activeShifts.map(toRow)}
          onJoin={allowJoin ? (shift) => void handleJoin(shift) : undefined}
          onLeave={(shift) => void handleLeave(shift)}
          emptyLabel='Nincs közelgő, szabad műszak.'
        />
      </div>

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Betelt és folyamatban lévő műszakok</h3>
        <SubmitShiftsContainer
          shifts={data.fullShifts.map(toRow)}
          onJoin={allowJoin ? (shift) => void handleJoin(shift) : undefined}
          onLeave={(shift) => void handleLeave(shift)}
          emptyLabel='Nincs betelt vagy folyamatban lévő műszak.'
        />
      </div>
    </main>
  );
}
