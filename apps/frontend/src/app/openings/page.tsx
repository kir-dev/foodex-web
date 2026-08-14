'use client';

import { useAuth } from '@/components/auth-provider';
import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { apiFetch, isApiError } from '@/lib/api';
import {
  formatLongDate,
  formatTime,
  toDateInputValue,
  toLocalDateTimePayload,
  toTimeInputValue,
} from '@/lib/dates';
import { DetailedShiftDto, UpdateShiftDto } from '@/types/api';
import { useCallback, useEffect, useState } from 'react';

export default function OpeningsPage() {
  return (
    <RequireAuth loadingLabel='Nyitások betöltése...'>
      <OpeningsContent />
    </RequireAuth>
  );
}

function OpeningsContent() {
  const { isAdminUser } = useAuth();
  const [openings, setOpenings] = useState<DetailedShiftDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [listMessage, setListMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [editingShift, setEditingShift] = useState<DetailedShiftDto | null>(null);
  const [editDate, setEditDate] = useState('');
  const [editStartTime, setEditStartTime] = useState('');
  const [editEndTime, setEditEndTime] = useState('');
  const [editPlace, setEditPlace] = useState('');
  const [editComment, setEditComment] = useState('');
  const [editMaxMembers, setEditMaxMembers] = useState(20);
  const [isSavingEdit, setIsSavingEdit] = useState(false);

  const loadOpenings = useCallback(async (): Promise<void> => {
    const data = await apiFetch<DetailedShiftDto[]>('/api/openings');
    setOpenings(Array.isArray(data) ? data : []);
  }, []);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        await loadOpenings();
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült lekérni az elfogadott nyitásokat.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [loadOpenings]);

  const handleOpenEdit = (shift: DetailedShiftDto): void => {
    setEditingShift(shift);
    setEditDate(toDateInputValue(shift.opening));
    setEditStartTime(toTimeInputValue(shift.opening));
    setEditEndTime(toTimeInputValue(shift.closing));
    setEditPlace(shift.place);
    setEditComment(shift.comment || '');
    setEditMaxMembers(shift.maxMembers || 20);
    setListMessage(null);
  };

  const handleSaveEdit = async (): Promise<void> => {
    if (!editingShift) {
      return;
    }
    if (!editDate || !editStartTime || !editEndTime || !editPlace.trim() || editMaxMembers < 1) {
      setListMessage({ text: 'Kérlek tölts ki minden kötelező mezőt!', isError: true });
      return;
    }

    setIsSavingEdit(true);
    setListMessage(null);
    try {
      const payload: UpdateShiftDto = {
        maxMembers: editMaxMembers,
        opening: toLocalDateTimePayload(editDate, editStartTime),
        closing: toLocalDateTimePayload(editDate, editEndTime),
        place: editPlace.trim(),
        comment: editComment.trim(),
      };
      const updated = await apiFetch<DetailedShiftDto>(`/api/openings/${editingShift.id}`, {
        method: 'PATCH',
        body: payload,
      });
      setOpenings((prev) => prev.map((shift) => (shift.id === updated.id ? updated : shift)));
      setEditingShift(null);
      setListMessage({ text: 'Nyitás módosítva.', isError: false });
    } catch (err) {
      setListMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült módosítani a nyitást.',
        isError: true,
      });
    } finally {
      setIsSavingEdit(false);
    }
  };

  const handleDelete = async (shift: DetailedShiftDto): Promise<void> => {
    if (!confirm('Biztosan törölni szeretnéd ezt a nyitást?')) {
      return;
    }

    setListMessage(null);
    try {
      await apiFetch<void>(`/api/openings/${shift.id}`, {
        method: 'DELETE',
        parseJson: false,
      });
      setOpenings((prev) => prev.filter((item) => item.id !== shift.id));
      if (editingShift?.id === shift.id) {
        setEditingShift(null);
      }
      setListMessage({ text: 'Nyitás törölve.', isError: false });
    } catch (err) {
      setListMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült törölni a nyitást.',
        isError: true,
      });
    }
  };

  if (loading) {
    return <PageState>Nyitások betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba: {error}</PageState>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center bg-white min-h-screen gap-6'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6'>
        <h1 className='text-3xl font-bold text-[#332C81] mb-6 pl-2'>Minden Elfogadott Nyitás (Múlt és Jövő)</h1>

        {listMessage && (
          <p className={`text-lg font-medium mb-4 pl-2 ${listMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
            {listMessage.text}
          </p>
        )}

        {openings.length === 0 ? (
          <p className='text-gray-500 text-lg pl-2'>Nincsenek elfogadott nyitások az adatbázisban.</p>
        ) : (
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {openings.map((shift) => (
              <div
                key={shift.id}
                className='border-2 border-[#332C81] bg-white rounded-xl p-4 flex flex-col justify-between hover:shadow-md transition-shadow'
              >
                <div>
                  <div className='flex justify-between items-start mb-2'>
                    <h3 className='text-2xl font-bold text-[#332C81]'>
                      {shift.cookingClub?.name || `Kör #${shift.id}`}
                    </h3>
                    <span className='bg-green-100 text-green-800 text-xs font-semibold px-2.5 py-0.5 rounded-full'>
                      Elfogadva
                    </span>
                  </div>

                  <p className='text-gray-700 font-medium mb-1'>📅 {formatLongDate(shift.opening)}</p>
                  <p className='text-gray-600 mb-1'>
                    🕒 {formatTime(shift.opening)} - {formatTime(shift.closing)}
                  </p>
                  <p className='text-gray-600 mb-2'>
                    📍 Helyszín: <span className='font-semibold text-black'>{shift.place}</span>
                  </p>

                  {shift.comment && (
                    <p className='text-sm text-gray-500 italic border-t pt-2 mt-2'>&quot;{shift.comment}&quot;</p>
                  )}
                </div>

                <div
                  className={`mt-4 border-t pt-2 ${
                    isAdminUser
                      ? 'flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2'
                      : 'text-right'
                  }`}
                >
                  <div className='text-xs text-gray-400'>
                    Létszám: {shift.members.length + shift.newbies.length} / {shift.maxMembers}
                  </div>
                  {isAdminUser && (
                    <div className='flex gap-2'>
                      <Button label='Módosítás' variant='secondary' onClick={() => handleOpenEdit(shift)} />
                      <Button label='Törlés' variant='secondary' onClick={() => void handleDelete(shift)} />
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {isAdminUser && editingShift && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
          <div className='bg-white rounded-xl border-2 border-[#332C81] p-6 max-w-md w-full space-y-4 shadow-xl'>
            <h4 className='text-2xl font-bold text-[#332C81]'>Nyitás módosítása</h4>
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
            <div className='flex justify-end gap-3 pt-2'>
              <Button
                label='Mégse'
                variant='secondary'
                onClick={() => setEditingShift(null)}
                disabled={isSavingEdit}
              />
              <Button
                label={isSavingEdit ? 'Mentés...' : 'Mentés'}
                variant='primary'
                onClick={() => void handleSaveEdit()}
                disabled={isSavingEdit}
              />
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
