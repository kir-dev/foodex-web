'use client';

import { ApprovedShiftsContainer } from '@/components/approvedShiftsContainer';
import Button from '@/components/button';
import { IncomingRequestsContainer } from '@/components/incomingRequestsContainer';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { Shift } from '@/components/ShiftTable';
import { apiFetch, isApiError } from '@/lib/api';
import { shiftCountFromRange, toDateInputValue, toLocalDateTimePayload, toTimeInputValue } from '@/lib/dates';
import { requestToRow, shiftToRow } from '@/lib/shift-view';
import {
  CreateShiftFromOpeningRequestDto,
  DetailedOpeningRequestDto,
  DetailedShiftDto,
  isClubLeaderOrAdmin,
  UpdateOpeningRequestDto,
  UpdateShiftDto,
} from '@/types/api';
import { useCallback, useEffect, useState } from 'react';

export default function RequestsPage() {
  return (
    <RequireAuth allow={isClubLeaderOrAdmin} loadingLabel='Kérések és műszakok betöltése...'>
      <RequestsContent />
    </RequireAuth>
  );
}

function RequestsContent() {
  const [requests, setRequests] = useState<DetailedOpeningRequestDto[]>([]);
  const [acceptedShifts, setAcceptedShifts] = useState<DetailedShiftDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [editingShift, setEditingShift] = useState<DetailedShiftDto | null>(null);
  const [editMaxMembers, setEditMaxMembers] = useState(20);
  const [editPlace, setEditPlace] = useState('');
  const [editComment, setEditComment] = useState('');
  const [isSavingShift, setIsSavingShift] = useState(false);
  const [defaultMaxMembers, setDefaultMaxMembers] = useState(20);

  const [editingRequest, setEditingRequest] = useState<DetailedOpeningRequestDto | null>(null);
  const [editRequestDate, setEditRequestDate] = useState('');
  const [editRequestStart, setEditRequestStart] = useState('');
  const [editRequestEnd, setEditRequestEnd] = useState('');
  const [editRequestPlace, setEditRequestPlace] = useState('');
  const [editRequestDescription, setEditRequestDescription] = useState('');
  const [isSavingRequest, setIsSavingRequest] = useState(false);

  const loadData = useCallback(async (): Promise<void> => {
    const [requestsData, openingsData] = await Promise.all([
      apiFetch<DetailedOpeningRequestDto[]>('/api/incoming-requests'),
      apiFetch<DetailedShiftDto[]>('/api/openings'),
    ]);
    setRequests(Array.isArray(requestsData) ? requestsData : []);
    setAcceptedShifts(Array.isArray(openingsData) ? openingsData : []);
  }, []);

  useEffect(() => {
    const fetchData = async (): Promise<void> => {
      try {
        await loadData();
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a kéréseket.');
      } finally {
        setLoading(false);
      }
    };

    void fetchData();
  }, [loadData]);

  const handleAcceptRequest = async (requestId: number): Promise<void> => {
    setActionMessage(null);
    try {
      const targetRequest = requests.find((request) => request.id === requestId);
      const payload: CreateShiftFromOpeningRequestDto = {
        maxMembers: Math.max(1, defaultMaxMembers),
        numberOfShifts: targetRequest ? shiftCountFromRange(targetRequest.opening, targetRequest.closing) : 1,
      };

      const newShifts = await apiFetch<DetailedShiftDto[]>(`/api/requests/${requestId}`, {
        method: 'POST',
        body: payload,
      });

      setRequests((prev) => prev.filter((req) => req.id !== requestId));
      setAcceptedShifts((prev) => [...prev, ...(Array.isArray(newShifts) ? newShifts : [])]);
      setActionMessage({ text: 'Kérés elfogadva, műszakok létrehozva.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült elfogadni a kérést.',
        isError: true,
      });
    }
  };

  const handleRejectRequest = async (requestId: number): Promise<void> => {
    if (!confirm('Biztosan elutasítod / törlöd ezt a kérést?')) {
      return;
    }

    setActionMessage(null);
    try {
      await apiFetch<void>(`/api/incoming-requests/${requestId}`, {
        method: 'DELETE',
        parseJson: false,
      });
      setRequests((prev) => prev.filter((req) => req.id !== requestId));
      setActionMessage({ text: 'Kérés elutasítva.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült elutasítani a kérést.',
        isError: true,
      });
    }
  };

  const handleOpenRequestEdit = (requestId: number): void => {
    const request = requests.find((item) => item.id === requestId);
    if (!request) {
      return;
    }
    setEditingRequest(request);
    setEditRequestDate(toDateInputValue(request.opening));
    setEditRequestStart(toTimeInputValue(request.opening));
    setEditRequestEnd(toTimeInputValue(request.closing));
    setEditRequestPlace(request.place);
    setEditRequestDescription(request.description);
  };

  const handleSaveRequest = async (): Promise<void> => {
    if (!editingRequest) {
      return;
    }
    setIsSavingRequest(true);
    setActionMessage(null);
    try {
      const payload: UpdateOpeningRequestDto = {
        opening: toLocalDateTimePayload(editRequestDate, editRequestStart),
        closing: toLocalDateTimePayload(editRequestDate, editRequestEnd),
        place: editRequestPlace,
        description: editRequestDescription,
      };
      const updated = await apiFetch<DetailedOpeningRequestDto>(`/api/incoming-requests/${editingRequest.id}`, {
        method: 'PATCH',
        body: payload,
      });
      setRequests((prev) => prev.map((req) => (req.id === updated.id ? updated : req)));
      setEditingRequest(null);
      setActionMessage({ text: 'Kérés módosítva.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült módosítani a kérést.',
        isError: true,
      });
    } finally {
      setIsSavingRequest(false);
    }
  };

  const handleOpenEditModal = (shiftRow: Shift): void => {
    const fullShift = acceptedShifts.find((shift) => shift.id === shiftRow.id);
    if (!fullShift) {
      return;
    }

    setEditingShift(fullShift);
    setEditMaxMembers(fullShift.maxMembers || 20);
    setEditPlace(fullShift.place || '');
    setEditComment(fullShift.comment || '');
  };

  const handleSaveShift = async (): Promise<void> => {
    if (!editingShift) {
      return;
    }

    setIsSavingShift(true);
    setActionMessage(null);
    try {
      const payload: UpdateShiftDto = {
        maxMembers: editMaxMembers,
        place: editPlace,
        comment: editComment,
      };
      const updatedShift = await apiFetch<DetailedShiftDto>(`/api/openings/${editingShift.id}`, {
        method: 'PATCH',
        body: payload,
      });

      setAcceptedShifts((prev) => prev.map((shift) => (shift.id === updatedShift.id ? updatedShift : shift)));
      setEditingShift(null);
      setActionMessage({ text: 'Műszak mentve.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Hiba a mentés során.',
        isError: true,
      });
    } finally {
      setIsSavingShift(false);
    }
  };

  const handleDeleteShift = async (shiftRow: Shift): Promise<void> => {
    if (!confirm('Biztosan törölni szeretnéd ezt a műszakot?')) {
      return;
    }

    setActionMessage(null);
    try {
      await apiFetch<void>(`/api/openings/${shiftRow.id}`, {
        method: 'DELETE',
        parseJson: false,
      });
      setAcceptedShifts((prev) => prev.filter((shift) => shift.id !== shiftRow.id));
      setActionMessage({ text: 'Műszak törölve.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Hiba a törlés során.',
        isError: true,
      });
    }
  };

  if (loading) {
    return <PageState>Kérések és műszakok betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba történt: {error}</PageState>;
  }

  return (
    <main className='p-6 flex flex-col items-center gap-6 bg-white min-h-screen'>
      <div className='w-full max-w-5xl flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3'>
        <label className='font-semibold text-[#332C81] flex items-center gap-2'>
          Alapértelmezett max. létszám:
          <input
            type='number'
            min={1}
            className='border-2 border-gray-300 rounded-lg p-2 text-black w-24'
            value={defaultMaxMembers}
            onChange={(e) => setDefaultMaxMembers(Number(e.target.value))}
          />
        </label>
        {actionMessage && (
          <span className={`text-lg font-medium ${actionMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
            {actionMessage.text}
          </span>
        )}
      </div>

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3 mb-2'>Bejövő kérések</h3>
        <IncomingRequestsContainer
          requests={requests.map(requestToRow)}
          onAccept={(id) => void handleAcceptRequest(id)}
          onReject={(id) => void handleRejectRequest(id)}
          onEdit={handleOpenRequestEdit}
        />
      </div>

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3 mb-2'>Elfogadott műszakok</h3>
        <ApprovedShiftsContainer
          shifts={acceptedShifts.map((shift) => shiftToRow(shift))}
          onEdit={handleOpenEditModal}
          onDelete={(shift) => void handleDeleteShift(shift)}
        />
      </div>

      {editingRequest && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
          <div className='bg-white rounded-xl border-2 border-[#332C81] p-6 max-w-md w-full space-y-4 shadow-xl'>
            <h4 className='text-2xl font-bold text-[#332C81]'>Kérés módosítása</h4>
            <p className='text-gray-600 font-medium'>{editingRequest.cookingClub?.name}</p>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Nap:</label>
              <input
                type='date'
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editRequestDate}
                onChange={(e) => setEditRequestDate(e.target.value)}
              />
            </div>
            <div className='flex gap-3'>
              <div className='flex flex-col gap-1 flex-1'>
                <label className='font-semibold text-[#332C81]'>Kezdés:</label>
                <input
                  type='time'
                  className='border-2 border-gray-300 rounded-lg p-2 text-black'
                  value={editRequestStart}
                  onChange={(e) => setEditRequestStart(e.target.value)}
                />
              </div>
              <div className='flex flex-col gap-1 flex-1'>
                <label className='font-semibold text-[#332C81]'>Vége:</label>
                <input
                  type='time'
                  className='border-2 border-gray-300 rounded-lg p-2 text-black'
                  value={editRequestEnd}
                  onChange={(e) => setEditRequestEnd(e.target.value)}
                />
              </div>
            </div>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Helyszín:</label>
              <input
                type='text'
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editRequestPlace}
                onChange={(e) => setEditRequestPlace(e.target.value)}
              />
            </div>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Leírás:</label>
              <textarea
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editRequestDescription}
                onChange={(e) => setEditRequestDescription(e.target.value)}
              />
            </div>
            <div className='flex justify-end gap-3 pt-2'>
              <Button
                label='Mégse'
                variant='secondary'
                onClick={() => setEditingRequest(null)}
                disabled={isSavingRequest}
              />
              <Button
                label={isSavingRequest ? 'Mentés...' : 'Mentés'}
                variant='primary'
                onClick={() => void handleSaveRequest()}
                disabled={isSavingRequest}
              />
            </div>
          </div>
        </div>
      )}

      {editingShift && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
          <div className='bg-white rounded-xl border-2 border-[#332C81] p-6 max-w-md w-full space-y-4 shadow-xl'>
            <h4 className='text-2xl font-bold text-[#332C81]'>Műszak módosítása</h4>
            <p className='text-gray-600 font-medium'>
              {editingShift.cookingClub?.name} – {new Date(editingShift.opening).toLocaleDateString('hu-HU')}
            </p>

            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Max. létszám:</label>
              <input
                type='number'
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={editMaxMembers}
                onChange={(e) => setEditMaxMembers(Number(e.target.value))}
              />
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
                disabled={isSavingShift}
              />
              <Button
                label={isSavingShift ? 'Mentés...' : 'Mentés'}
                variant='primary'
                onClick={() => void handleSaveShift()}
                disabled={isSavingShift}
              />
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
