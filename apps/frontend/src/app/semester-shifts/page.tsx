'use client';

import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { Shift, ShiftTable } from '@/components/ShiftTable';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';
import { TimeInput } from '@/components/timeInput';
import { apiFetch, isApiError } from '@/lib/api';
import {
  compareByOpeningDesc,
  formatShortDate,
  formatTimeRange,
  formatWeekday,
  toDateInputValue,
  toLocalDateTimePayload,
  toTimeInputValue,
} from '@/lib/dates';
import { shiftToRow } from '@/lib/shift-view';
import { useRefetchOnPath } from '@/lib/use-refetch-on-path';
import {
  CreateShiftDto,
  DetailedOpeningRequestDto,
  DetailedShiftDto,
  DetailedUserDto,
  isAdmin,
  Role,
  UpdateShiftDto,
  UserDto,
} from '@/types/api';
import { useCallback, useMemo, useState } from 'react';

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
  const [openingRequests, setOpeningRequests] = useState<DetailedOpeningRequestDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [openingRequestId, setOpeningRequestId] = useState<number | ''>('');
  const [cookingClubId, setCookingClubId] = useState<number | ''>('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [location, setLocation] = useState('');
  const [comment, setComment] = useState('');
  const [maxMembers, setMaxMembers] = useState(6);
  const [creating, setCreating] = useState(false);
  const [formMessage, setFormMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [editingShift, setEditingShift] = useState<DetailedShiftDto | null>(null);
  const [editDate, setEditDate] = useState('');
  const [editStartTime, setEditStartTime] = useState('');
  const [editEndTime, setEditEndTime] = useState('');
  const [editPlace, setEditPlace] = useState('');
  const [editComment, setEditComment] = useState('');
  const [editMaxMembers, setEditMaxMembers] = useState(6);
  const [isSaving, setIsSaving] = useState(false);
  const [selectedWorkerId, setSelectedWorkerId] = useState<number | ''>('');
  const [draftWorkers, setDraftWorkers] = useState<UserDto[]>([]);

  const loadData = useCallback(async (): Promise<void> => {
    const [shiftData, userData, requestData] = await Promise.all([
      apiFetch<DetailedShiftDto[]>('/api/semester-shifts'),
      apiFetch<DetailedUserDto[]>('/api/users'),
      apiFetch<DetailedOpeningRequestDto[]>('/api/semester-openings'),
    ]);
    setShifts(Array.isArray(shiftData) ? shiftData : []);
    setUsers(Array.isArray(userData) ? userData : []);
    setOpeningRequests(Array.isArray(requestData) ? requestData : []);
  }, []);

  useRefetchOnPath(async () => {
    try {
      await loadData();
    } catch (err) {
      setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a féléves műszakokat.');
    } finally {
      setLoading(false);
    }
  });

  const handleCreate = async (): Promise<void> => {
    setFormMessage(null);
    if (
      openingRequestId === '' ||
      cookingClubId === '' ||
      !date ||
      !startTime ||
      !endTime ||
      !location.trim() ||
      maxMembers < 1 ||
      maxMembers > 6
    ) {
      setFormMessage({ text: 'Kérlek tölts ki minden kötelező mezőt! A max. létszám 1 és 6 között legyen.', isError: true });
      return;
    }

    const linkedCount = shifts.filter((shift) => shift.openingRequestId === openingRequestId).length;
    if (linkedCount >= 4) {
      setFormMessage({ text: 'Ehhez a nyitáshoz már 4 műszak tartozik.', isError: true });
      return;
    }

    setCreating(true);
    try {
      const payload: CreateShiftDto = {
        cookingClubId: Number(cookingClubId),
        openingRequestId: Number(openingRequestId),
        maxMembers,
        opening: toLocalDateTimePayload(date, startTime),
        closing: toLocalDateTimePayload(date, endTime),
        place: location.trim(),
        comment: comment.trim(),
      };
      await apiFetch<DetailedShiftDto>('/api/semester-shifts', {
        method: 'POST',
        body: payload,
      });
      setOpeningRequestId('');
      setCookingClubId('');
      setDate('');
      setStartTime('');
      setEndTime('');
      setLocation('');
      setComment('');
      setMaxMembers(6);
      await loadData();
      setFormMessage({
        text: 'Műszak létrehozva. Ha nem jelenik meg a listában, ellenőrizd a félév dátumait a Konfig oldalon.',
        isError: false,
      });
    } catch (err) {
      setFormMessage({
        text:
          isApiError(err) && err.message.toLowerCase().includes('maximum')
            ? 'Ehhez a nyitáshoz már 4 műszak tartozik.'
            : isApiError(err)
              ? err.message
              : 'Nem sikerült létrehozni a műszakot.',
        isError: true,
      });
    } finally {
      setCreating(false);
    }
  };

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
    setEditMaxMembers(fullShift.maxMembers || 6);
    setSelectedWorkerId('');
    setDraftWorkers([...fullShift.members, ...fullShift.newbies]);
    setActionMessage(null);
  };

  const handleSave = async (): Promise<void> => {
    if (!editingShift) {
      return;
    }
    if (!editDate || !editStartTime || !editEndTime || !editPlace.trim() || editMaxMembers < 1 || editMaxMembers > 6) {
      setActionMessage({ text: 'Kérlek tölts ki minden kötelező mezőt! A max. létszám 1 és 6 között legyen.', isError: true });
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
      latest = await apiFetch<DetailedShiftDto>(`/api/semester-shifts/${editingShift.id}`, {
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
      await apiFetch<void>(`/api/semester-shifts/${shiftRow.id}`, {
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

  const linkableRequests = useMemo(() => {
    return openingRequests
      .filter((request) => {
        const linked = shifts.filter((shift) => shift.openingRequestId === request.id).length;
        return linked < 4;
      })
      .sort(compareByOpeningDesc);
  }, [openingRequests, shifts]);

  const handleSelectOpeningRequest = (requestId: number | ''): void => {
    setOpeningRequestId(requestId);
    if (requestId === '') {
      setCookingClubId('');
      return;
    }
    const request = openingRequests.find((item) => item.id === requestId);
    setCookingClubId(request?.cookingClub?.id ?? '');
  };

  if (loading) {
    return <PageState>Féléves műszakok betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba: {error}</PageState>;
  }

  return (
    <main className='p-6 flex flex-col items-center gap-6 bg-white flex-1'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6 space-y-4'>
        <h2 className='text-2xl font-bold text-[#332C81]'>Új műszak létrehozása</h2>
        <p className='text-[#332C81]'>A műszakot egy nyitási kéréshez kell rendelni (legfeljebb 4 műszak / nyitás).</p>

        <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
          <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860]'>
            <StyledLabel>Nyitási kérés</StyledLabel>
            <select
              className='bg-white p-2 rounded-2xl text-black text-xl mt-2 w-full'
              value={openingRequestId}
              onChange={(e) => handleSelectOpeningRequest(e.target.value ? Number(e.target.value) : '')}
            >
              <option value=''>Válassz nyitási kérést</option>
              {linkableRequests.map((request) => {
                const linked = shifts.filter((shift) => shift.openingRequestId === request.id).length;
                return (
                  <option key={request.id} value={request.id}>
                    {request.cookingClub?.name || `Kör #${request.cookingClub?.id ?? request.id}`} ·{' '}
                    {formatWeekday(request.opening)} {formatShortDate(request.opening)} ·{' '}
                    {formatTimeRange(request.opening, request.closing)} ({linked}/4)
                  </option>
                );
              })}
            </select>
          </div>
          <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860]'>
            <StyledLabel>Max. létszám</StyledLabel>
            <StyledInput
              type='number'
              min={1}
              max={6}
              value={maxMembers}
              onChange={(e) => setMaxMembers(Number(e.target.value))}
            />
          </div>
        </div>

        <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860]'>
          <StyledLabel>Műszak</StyledLabel>
          <div className='flex flex-col sm:flex-row sm:flex-wrap gap-4 sm:gap-6 items-start sm:items-center w-full'>
            <div className='flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3.5 w-full sm:w-auto'>
              <StyledLabel>Napja:</StyledLabel>
              <StyledInput type='date' size='large' value={date} onChange={(e) => setDate(e.target.value)} />
            </div>

            <div className='flex flex-col sm:flex-row sm:items-center gap-2 w-full sm:w-auto'>
              <StyledLabel>Ideje:</StyledLabel>
              <div className='flex items-center gap-2 w-full sm:w-auto text-black'>
                <TimeInput className='text-[#ff9860]' value={startTime} onChange={setStartTime} />
                <span className='mx-1 text-[#ff9860] font-semibold'>–</span>
                <TimeInput className='text-[#ff9860]' value={endTime} onChange={setEndTime} />
              </div>
            </div>

            <div className='flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3.5 w-full sm:w-auto'>
              <StyledLabel>Helye:</StyledLabel>
              <StyledInput
                type='text'
                placeholder='pl. 13. konyha'
                size='medium'
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              />
            </div>
          </div>
        </div>

        <div className='bg-[#2f2173] text-white p-4 rounded-2xl border-2 border-[#ff9860]'>
          <StyledLabel>Megjegyzés</StyledLabel>
          <textarea
            className='bg-white w-full p-3 rounded-2xl text-black text-xl h-24 mt-3'
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          />
        </div>

        <div className='flex flex-col sm:flex-row sm:items-center gap-4'>
          <Button
            label={creating ? 'Létrehozás...' : 'Műszak létrehozása'}
            variant='primary'
            onClick={() => void handleCreate()}
            disabled={creating}
          />
          {formMessage && (
            <span className={`text-lg font-medium ${formMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
              {formMessage.text}
            </span>
          )}
        </div>
      </div>

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
                <TimeInput className='text-[#ff9860]' value={editStartTime} onChange={setEditStartTime} />
              </div>
              <div className='flex flex-col gap-1 flex-1'>
                <label className='font-semibold text-[#332C81]'>Vége:</label>
                <TimeInput className='text-[#ff9860]' value={editEndTime} onChange={setEditEndTime} />
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
                max={6}
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
