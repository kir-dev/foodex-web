'use client';

import { useAuth } from '@/components/auth-provider';
import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequestShiftsModal } from '@/components/requestShiftsModal';
import { RequireAuth } from '@/components/require-auth';
import { TimeInput } from '@/components/timeInput';
import { apiFetch, isApiError } from '@/lib/api';
import {
  compareByOpeningDesc,
  formatLongDate,
  formatTime,
  toDateInputValue,
  toLocalDateTimePayload,
  toTimeInputValue,
} from '@/lib/dates';
import { useRefetchOnPath } from '@/lib/use-refetch-on-path';
import { DetailedOpeningRequestDto, UpdateOpeningRequestDto } from '@/types/api';
import { useCallback, useMemo, useState } from 'react';

function isRequestAccepted(request: DetailedOpeningRequestDto): boolean {
  return request.accepted ?? request.isAccepted ?? false;
}

export default function OpeningsPage() {
  return (
    <RequireAuth loadingLabel='Nyitások betöltése...'>
      <OpeningsContent />
    </RequireAuth>
  );
}

function OpeningsContent() {
  const { isAdminUser, refresh } = useAuth();
  const [openings, setOpenings] = useState<DetailedOpeningRequestDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [listMessage, setListMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [editingRequest, setEditingRequest] = useState<DetailedOpeningRequestDto | null>(null);
  const [editDate, setEditDate] = useState('');
  const [editStartTime, setEditStartTime] = useState('');
  const [editEndTime, setEditEndTime] = useState('');
  const [editPlace, setEditPlace] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [isSavingEdit, setIsSavingEdit] = useState(false);
  const [shiftsRequest, setShiftsRequest] = useState<DetailedOpeningRequestDto | null>(null);

  const loadOpenings = useCallback(async (): Promise<void> => {
    const data = await apiFetch<DetailedOpeningRequestDto[]>('/api/semester-openings');
    setOpenings(Array.isArray(data) ? data : []);
  }, []);

  const orderedOpenings = useMemo(
    () => [...openings].sort(compareByOpeningDesc),
    [openings]
  );

  useRefetchOnPath(async () => {
    try {
      await loadOpenings();
    } catch (err) {
      setError(isApiError(err) ? err.message : 'Nem sikerült lekérni a féléves nyitásokat.');
    } finally {
      setLoading(false);
    }
  });

  const handleOpenEdit = (request: DetailedOpeningRequestDto): void => {
    setEditingRequest(request);
    setEditDate(toDateInputValue(request.opening));
    setEditStartTime(toTimeInputValue(request.opening));
    setEditEndTime(toTimeInputValue(request.closing));
    setEditPlace(request.place);
    setEditDescription(request.description || '');
    setListMessage(null);
  };

  const handleSaveEdit = async (): Promise<void> => {
    if (!editingRequest) {
      return;
    }
    if (!editDate || !editStartTime || !editEndTime || !editPlace.trim()) {
      setListMessage({ text: 'Kérlek tölts ki minden kötelező mezőt!', isError: true });
      return;
    }

    setIsSavingEdit(true);
    setListMessage(null);
    try {
      const payload: UpdateOpeningRequestDto = {
        opening: toLocalDateTimePayload(editDate, editStartTime),
        closing: toLocalDateTimePayload(editDate, editEndTime),
        place: editPlace.trim(),
        description: editDescription.trim(),
      };
      const updated = await apiFetch<DetailedOpeningRequestDto>(`/api/incoming-requests/${editingRequest.id}`, {
        method: 'PATCH',
        body: payload,
      });
      setOpenings((prev) => prev.map((request) => (request.id === updated.id ? updated : request)));
      setEditingRequest(null);
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

  const handleDelete = async (request: DetailedOpeningRequestDto): Promise<void> => {
    if (!confirm('Biztosan törölni szeretnéd ezt a nyitást? A hozzá tartozó műszakok is törlődnek.')) {
      return;
    }

    setListMessage(null);
    try {
      await apiFetch<void>(`/api/incoming-requests/${request.id}`, {
        method: 'DELETE',
        parseJson: false,
      });
      setOpenings((prev) => prev.filter((item) => item.id !== request.id));
      if (editingRequest?.id === request.id) {
        setEditingRequest(null);
      }
      await refresh();
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
    <main className='p-4 sm:p-8 flex flex-col items-center bg-white flex-1 gap-6'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6'>
        <h1 className='text-3xl font-bold text-[#332C81] mb-6 pl-2'>Féléves Nyitások</h1>

        {listMessage && (
          <p className={`text-lg font-medium mb-4 pl-2 ${listMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
            {listMessage.text}
          </p>
        )}

        {openings.length === 0 ? (
          <p className='text-gray-500 text-lg pl-2'>Nincsenek nyitási kérések ebben a félévben.</p>
        ) : (
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {orderedOpenings.map((request) => {
              const accepted = isRequestAccepted(request);
              return (
                <div
                  key={request.id}
                  className='border-2 border-[#332C81] bg-white rounded-xl p-4 flex flex-col justify-between hover:shadow-md transition-shadow'
                >
                  <div>
                    <div className='flex justify-between items-start mb-2'>
                      <h3 className='text-2xl font-bold text-[#332C81]'>
                        {request.cookingClub?.name || `Kör #${request.id}`}
                      </h3>
                      <span
                        className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${
                          accepted ? 'bg-green-100 text-green-800' : 'bg-orange-100 text-[#332C81]'
                        }`}
                      >
                        {accepted ? 'Elfogadva' : 'Függőben'}
                      </span>
                    </div>

                    <p className='text-gray-700 font-medium mb-1'>📅 {formatLongDate(request.opening)}</p>
                    <p className='text-gray-600 mb-1'>
                      🕒 {formatTime(request.opening)} - {formatTime(request.closing)}
                    </p>
                    <p className='text-gray-600 mb-2'>
                      📍 Helyszín: <span className='font-semibold text-black'>{request.place}</span>
                    </p>

                    {request.description && (
                      <p className='text-sm text-gray-500 italic border-t pt-2 mt-2'>&quot;{request.description}&quot;</p>
                    )}
                  </div>

                  <div
                    className={`mt-4 border-t pt-2 ${
                      isAdminUser || accepted
                        ? 'flex flex-col sm:flex-row sm:items-center sm:justify-end gap-2'
                        : ''
                    }`}
                  >
                    <div className='flex flex-wrap gap-2 justify-end'>
                      {accepted && (
                        <Button
                          label='Műszakok'
                          variant='secondary'
                          onClick={() => setShiftsRequest(request)}
                        />
                      )}
                      {isAdminUser && (
                        <>
                          <Button label='Módosítás' variant='secondary' onClick={() => handleOpenEdit(request)} />
                          <Button label='Törlés' variant='secondary' onClick={() => void handleDelete(request)} />
                        </>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {isAdminUser && editingRequest && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
          <div className='bg-white rounded-xl border-2 border-[#332C81] p-6 max-w-md w-full space-y-4 shadow-xl'>
            <h4 className='text-2xl font-bold text-[#332C81]'>Nyitás módosítása</h4>
            <p className='text-gray-600 font-medium'>{editingRequest.cookingClub?.name}</p>

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
                <TimeInput value={editStartTime} onChange={setEditStartTime} />
              </div>
              <div className='flex flex-col gap-1 flex-1'>
                <label className='font-semibold text-[#332C81]'>Vége:</label>
                <TimeInput value={editEndTime} onChange={setEditEndTime} />
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
              <label className='font-semibold text-[#332C81]'>Leírás:</label>
              <textarea
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editDescription}
                onChange={(e) => setEditDescription(e.target.value)}
              />
            </div>
            <div className='flex justify-end gap-3 pt-2'>
              <Button
                label='Mégse'
                variant='secondary'
                onClick={() => setEditingRequest(null)}
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

      {shiftsRequest && (
        <RequestShiftsModal
          requestId={shiftsRequest.id}
          clubName={shiftsRequest.cookingClub?.name}
          onClose={() => setShiftsRequest(null)}
        />
      )}
    </main>
  );
}
