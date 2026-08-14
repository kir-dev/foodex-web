'use client';

import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { Shift, ShiftTable } from '@/components/ShiftTable';
import { apiFetch, isApiError } from '@/lib/api';
import { toDateInputValue, toLocalDateTimePayload, toTimeInputValue } from '@/lib/dates';
import { shiftToRow } from '@/lib/shift-view';
import {
  DetailedShiftDto,
  DetailedUserDto,
  isAdmin,
  Role,
  UpdateShiftDto,
  UserDto,
} from '@/types/api';
import { useCallback, useEffect, useMemo, useState } from 'react';

const ROLE_LABEL: Record<Role, string> = {
  ADMIN: 'admin',
  MEMBER: 'tag',
  NEWBIE: 'újonc',
  GUEST: 'vendég',
};

export default function SemesterShiftsPage() {
  return (
    <RequireAuth allow={isAdmin} loadingLabel='Féléves műszakok betöltése...'>
      <SemesterShiftsContent />
    </RequireAuth>
  );
}

function SemesterShiftsContent() {
  const [shifts, setShifts] = useState<DetailedShiftDto[]>([]);
  const [users, setUsers] = useState<DetailedUserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [editingShift, setEditingShift] = useState<DetailedShiftDto | null>(null);
  const [editDate, setEditDate] = useState('');
  const [editStartTime, setEditStartTime] = useState('');
  const [editEndTime, setEditEndTime] = useState('');
  const [editPlace, setEditPlace] = useState('');
  const [editComment, setEditComment] = useState('');
  const [editMaxMembers, setEditMaxMembers] = useState(20);
  const [isSaving, setIsSaving] = useState(false);
  const [selectedWorkerId, setSelectedWorkerId] = useState<number | ''>('');
  const [draftWorkers, setDraftWorkers] = useState<UserDto[]>([]);

  const loadData = useCallback(async (): Promise<void> => {
    const [shiftData, userData] = await Promise.all([
      apiFetch<DetailedShiftDto[]>('/api/openings'),
      apiFetch<DetailedUserDto[]>('/api/users'),
    ]);
    setShifts(Array.isArray(shiftData) ? shiftData : []);
    setUsers(Array.isArray(userData) ? userData : []);
  }, []);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        await loadData();
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a féléves műszakokat.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [loadData]);

  const replaceShift = (updated: DetailedShiftDto): void => {
    setShifts((prev) => prev.map((shift) => (shift.id === updated.id ? updated : shift)));
  };

  const closeEditor = (): void => {
    setEditingShift(null);
    setDraftWorkers([]);
    setSelectedWorkerId('');
  };

  const handleOpenEdit = (shiftRow: Shift): void => {
    const fullShift = shifts.find((shift) => shift.id === shiftRow.id);
    if (!fullShift) {
      return;
    }
    setEditingShift(fullShift);
    setEditDate(toDateInputValue(fullShift.opening));
    setEditStartTime(toTimeInputValue(fullShift.opening));
    setEditEndTime(toTimeInputValue(fullShift.closing));
    setEditPlace(fullShift.place);
    setEditComment(fullShift.comment || '');
    setEditMaxMembers(fullShift.maxMembers || 20);
    setSelectedWorkerId('');
    setDraftWorkers([...fullShift.members, ...fullShift.newbies]);
    setActionMessage(null);
  };

  const handleSave = async (): Promise<void> => {
    if (!editingShift) {
      return;
    }
    if (!editDate || !editStartTime || !editEndTime || !editPlace.trim() || editMaxMembers < 1) {
      setActionMessage({ text: 'Kérlek tölts ki minden kötelező mezőt!', isError: true });
      return;
    }

    setIsSaving(true);
    setActionMessage(null);
    try {
      const originalIds = new Set(
        [...editingShift.members, ...editingShift.newbies].map((worker) => worker.id)
      );
      const draftIds = new Set(draftWorkers.map((worker) => worker.id));
      const toRemove = [...originalIds].filter((id) => !draftIds.has(id));
      const toAdd = [...draftIds].filter((id) => !originalIds.has(id));

      let latest = editingShift;
      for (const workerId of toRemove) {
        latest = await apiFetch<DetailedShiftDto>(`/api/shifts/${editingShift.id}/${workerId}`, {
          method: 'DELETE',
        });
      }
      for (const workerId of toAdd) {
        latest = await apiFetch<DetailedShiftDto>(`/api/shifts/${editingShift.id}/${workerId}`, {
          method: 'POST',
        });
      }

      const payload: UpdateShiftDto = {
        maxMembers: editMaxMembers,
        opening: toLocalDateTimePayload(editDate, editStartTime),
        closing: toLocalDateTimePayload(editDate, editEndTime),
        place: editPlace.trim(),
        comment: editComment.trim(),
      };
      latest = await apiFetch<DetailedShiftDto>(`/api/openings/${editingShift.id}`, {
        method: 'PATCH',
        body: payload,
      });
      replaceShift(latest);
      closeEditor();
      setActionMessage({ text: 'Műszak mentve.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült menteni a műszakot.',
        isError: true,
      });
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (shiftRow: Shift): Promise<void> => {
    if (!confirm('Biztosan törölni szeretnéd ezt a műszakot?')) {
      return;
    }

    setActionMessage(null);
    try {
      await apiFetch<void>(`/api/openings/${shiftRow.id}`, {
        method: 'DELETE',
        parseJson: false,
      });
      setShifts((prev) => prev.filter((shift) => shift.id !== shiftRow.id));
      if (editingShift?.id === shiftRow.id) {
        closeEditor();
      }
      setActionMessage({ text: 'Műszak törölve.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült törölni a műszakot.',
        isError: true,
      });
    }
  };

  const handleRemoveWorker = (workerId: number): void => {
    setDraftWorkers((prev) => prev.filter((worker) => worker.id !== workerId));
  };

  const handleAddWorker = (): void => {
    if (selectedWorkerId === '') {
      return;
    }
    const user = users.find((item) => item.id === selectedWorkerId);
    if (!user || draftWorkers.some((worker) => worker.id === user.id)) {
      return;
    }
    setDraftWorkers((prev) => [
      ...prev,
      { id: user.id, role: user.role, nickname: user.nickname || user.name },
    ]);
    setSelectedWorkerId('');
  };

  const addableUsers = useMemo(() => {
    const draftIds = new Set(draftWorkers.map((worker) => worker.id));
    return users.filter((user) => user.role !== 'GUEST' && !draftIds.has(user.id));
  }, [draftWorkers, users]);

  if (loading) {
    return <PageState>Féléves műszakok betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba: {error}</PageState>;
  }

  return (
    <main className='p-6 flex flex-col items-center gap-6 bg-white min-h-screen'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <div className='flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 px-3 mb-2'>
          <h1 className='text-2xl font-bold text-[#332C81]'>Féléves műszakok</h1>
          {actionMessage && (
            <span className={`text-lg font-medium ${actionMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
              {actionMessage.text}
            </span>
          )}
        </div>
        <ShiftTable
          shifts={shifts.map((shift) => shiftToRow(shift))}
          showNamesColumn
          emptyLabel='Nincs műszak ebben a félévben.'
          buttons={[
            { label: 'Módosítás', onClick: handleOpenEdit },
            { label: 'Törlés', onClick: (shift) => void handleDelete(shift) },
          ]}
        />
      </div>

      {editingShift && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
          <div className='bg-white rounded-xl border-2 border-[#332C81] p-6 max-w-lg w-full space-y-4 shadow-xl max-h-[90vh] overflow-y-auto'>
            <h4 className='text-2xl font-bold text-[#332C81]'>Műszak módosítása</h4>
            <p className='text-gray-600 font-medium'>{editingShift.cookingClub?.name}</p>

            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Nap:</label>
              <input
                type='date'
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editDate}
                onChange={(e) => setEditDate(e.target.value)}
              />
            </div>
            <div className='flex gap-3'>
              <div className='flex flex-col gap-1 flex-1'>
                <label className='font-semibold text-[#332C81]'>Kezdés:</label>
                <input
                  type='time'
                  className='border-2 border-gray-300 rounded-lg p-2 text-black'
                  value={editStartTime}
                  onChange={(e) => setEditStartTime(e.target.value)}
                />
              </div>
              <div className='flex flex-col gap-1 flex-1'>
                <label className='font-semibold text-[#332C81]'>Vége:</label>
                <input
                  type='time'
                  className='border-2 border-gray-300 rounded-lg p-2 text-black'
                  value={editEndTime}
                  onChange={(e) => setEditEndTime(e.target.value)}
                />
              </div>
            </div>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Helyszín:</label>
              <input
                type='text'
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editPlace}
                onChange={(e) => setEditPlace(e.target.value)}
              />
            </div>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Max. létszám:</label>
              <input
                type='number'
                min={1}
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editMaxMembers}
                onChange={(e) => setEditMaxMembers(Number(e.target.value))}
              />
            </div>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Megjegyzés:</label>
              <textarea
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editComment}
                onChange={(e) => setEditComment(e.target.value)}
              />
            </div>

            <div className='space-y-2'>
              <h5 className='font-semibold text-[#332C81] text-lg'>Dolgozók</h5>
              {draftWorkers.length === 0 ? (
                <p className='text-gray-500 italic'>Még senki nincs a műszakon.</p>
              ) : (
                <ul className='flex flex-col gap-2'>
                  {draftWorkers.map((worker) => (
                    <li
                      key={worker.id}
                      className='flex items-center justify-between gap-2 border-2 border-[#332C81] rounded-lg px-3 py-2'
                    >
                      <span className='text-[#332C81] font-medium'>
                        {worker.nickname}{' '}
                        <span className='text-sm font-normal text-gray-500'>({ROLE_LABEL[worker.role]})</span>
                      </span>
                      <button
                        type='button'
                        className='bg-white text-[#332C81] font-bold px-3 py-1 rounded-xl border-2 border-[#332C81]'
                        onClick={() => handleRemoveWorker(worker.id)}
                        disabled={isSaving}
                      >
                        Eltávolítás
                      </button>
                    </li>
                  ))}
                </ul>
              )}

              <div className='flex flex-col sm:flex-row gap-2 pt-1'>
                <select
                  className='border-2 border-gray-300 rounded-lg p-2 text-black flex-1'
                  value={selectedWorkerId}
                  onChange={(e) => setSelectedWorkerId(e.target.value ? Number(e.target.value) : '')}
                >
                  <option value=''>Válassz dolgozót</option>
                  {addableUsers.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.nickname || user.name} ({ROLE_LABEL[user.role]})
                    </option>
                  ))}
                </select>
                <Button
                  label='Hozzáadás'
                  variant='secondary'
                  onClick={handleAddWorker}
                  disabled={isSaving || selectedWorkerId === ''}
                />
              </div>
            </div>

            {actionMessage?.isError && <p className='text-red-500 font-medium'>{actionMessage.text}</p>}

            <div className='flex justify-end gap-3 pt-2'>
              <Button label='Mégse' variant='secondary' onClick={closeEditor} disabled={isSaving} />
              <Button
                label={isSaving ? 'Mentés...' : 'Mentés'}
                variant='primary'
                onClick={() => void handleSave()}
                disabled={isSaving}
              />
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
