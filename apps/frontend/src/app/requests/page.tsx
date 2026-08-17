'use client';

import { ApprovedShiftsContainer } from '@/components/approvedShiftsContainer';
import { useAuth } from '@/components/auth-provider';
import Button from '@/components/button';
import { IncomingRequestsContainer } from '@/components/incomingRequestsContainer';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { Shift } from '@/components/ShiftTable';
import { TimeInput } from '@/components/timeInput';
import { apiFetch, isApiError } from '@/lib/api';
import {
  formatLongDate,
  formatTimeRange,
  shiftCountFromRange,
  toDateInputValue,
  toLocalDateTimePayload,
  toTimeInputValue,
} from '@/lib/dates';
import { requestToRow } from '@/lib/shift-view';
import { useRefetchOnPath } from '@/lib/use-refetch-on-path';
import {
  CreateShiftFromOpeningRequestDto,
  DetailedOpeningRequestDto,
  DetailedShiftDto,
  isClubLeaderOrAdmin,
  UpdateOpeningRequestDto,
} from '@/types/api';
import { useCallback, useState } from 'react';

export default function RequestsPage() {
  return (
    <RequireAuth allow={isClubLeaderOrAdmin} loadingLabel='Kérések betöltése...'>
      <RequestsContent />
    </RequireAuth>
  );
}

function RequestsContent() {
  const { refresh } = useAuth();
  const [requests, setRequests] = useState<DetailedOpeningRequestDto[]>([]);
  const [acceptedRequests, setAcceptedRequests] = useState<DetailedOpeningRequestDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const [editingRequest, setEditingRequest] = useState<DetailedOpeningRequestDto | null>(null);
  const [editRequestDate, setEditRequestDate] = useState('');
  const [editRequestStart, setEditRequestStart] = useState('');
  const [editRequestEnd, setEditRequestEnd] = useState('');
  const [editRequestPlace, setEditRequestPlace] = useState('');
  const [editRequestDescription, setEditRequestDescription] = useState('');
  const [isSavingRequest, setIsSavingRequest] = useState(false);

  const [acceptingRequest, setAcceptingRequest] = useState<DetailedOpeningRequestDto | null>(null);
  const [acceptShiftCount, setAcceptShiftCount] = useState(1);
  const [acceptMaxMembers, setAcceptMaxMembers] = useState(6);
  const [isAccepting, setIsAccepting] = useState(false);

  const loadData = useCallback(async (): Promise<void> => {
    const [requestsData, acceptedData] = await Promise.all([
      apiFetch<DetailedOpeningRequestDto[]>('/api/incoming-requests'),
      apiFetch<DetailedOpeningRequestDto[]>('/api/accepted-requests'),
    ]);
    setRequests(Array.isArray(requestsData) ? requestsData : []);
    setAcceptedRequests(Array.isArray(acceptedData) ? acceptedData : []);
  }, []);

  useRefetchOnPath(async () => {
    try {
      await loadData();
    } catch (err) {
      setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a kéréseket.');
    } finally {
      setLoading(false);
    }
  });

  const findRequest = (requestId: number): DetailedOpeningRequestDto | undefined =>
    requests.find((item) => item.id === requestId) ?? acceptedRequests.find((item) => item.id === requestId);

  const handleOpenAccept = (requestId: number): void => {
    const request = requests.find((item) => item.id === requestId);
    if (!request) {
      return;
    }
    setAcceptingRequest(request);
    setAcceptShiftCount(Math.min(4, shiftCountFromRange(request.opening, request.closing)));
    setAcceptMaxMembers(6);
    setActionMessage(null);
  };

  const handleConfirmAccept = async (): Promise<void> => {
    if (!acceptingRequest) {
      return;
    }
    if (!Number.isFinite(acceptShiftCount) || acceptShiftCount < 1 || acceptShiftCount > 4) {
      setActionMessage({ text: 'A műszakok száma 1 és 4 között legyen.', isError: true });
      return;
    }
    if (!Number.isFinite(acceptMaxMembers) || acceptMaxMembers < 1 || acceptMaxMembers > 6) {
      setActionMessage({ text: 'A max. létszám 1 és 6 között legyen.', isError: true });
      return;
    }

    setIsAccepting(true);
    setActionMessage(null);
    try {
      const payload: CreateShiftFromOpeningRequestDto = {
        maxMembers: Math.floor(acceptMaxMembers),
        numberOfShifts: Math.floor(acceptShiftCount),
      };

      await apiFetch<DetailedShiftDto[]>(`/api/requests/${acceptingRequest.id}`, {
        method: 'POST',
        body: payload,
      });

      setRequests((prev) => prev.filter((req) => req.id !== acceptingRequest.id));
      setAcceptedRequests((prev) => [...prev, { ...acceptingRequest, accepted: true, isAccepted: true }]);
      setAcceptingRequest(null);
      await refresh();
      setActionMessage({ text: 'Kérés elfogadva, műszakok létrehozva.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült elfogadni a kérést.',
        isError: true,
      });
    } finally {
      setIsAccepting(false);
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
      await refresh();
      setActionMessage({ text: 'Kérés elutasítva.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült elutasítani a kérést.',
        isError: true,
      });
    }
  };

  const handleOpenRequestEdit = (requestId: number): void => {
    const request = findRequest(requestId);
    if (!request) {
      return;
    }
    setEditingRequest(request);
    setEditRequestDate(toDateInputValue(request.opening));
    setEditRequestStart(toTimeInputValue(request.opening));
    setEditRequestEnd(toTimeInputValue(request.closing));
    setEditRequestPlace(request.place);
    setEditRequestDescription(request.description);
    setActionMessage(null);
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
      setAcceptedRequests((prev) => prev.map((req) => (req.id === updated.id ? updated : req)));
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

  const handleDeleteAcceptedRequest = async (row: Shift): Promise<void> => {
    if (!confirm('Biztosan törölni szeretnéd ezt a nyitást? A hozzá tartozó műszakok is törlődnek.')) {
      return;
    }

    setActionMessage(null);
    try {
      await apiFetch<void>(`/api/incoming-requests/${row.id}`, {
        method: 'DELETE',
        parseJson: false,
      });
      setAcceptedRequests((prev) => prev.filter((request) => request.id !== row.id));
      if (editingRequest?.id === row.id) {
        setEditingRequest(null);
      }
      await refresh();
      setActionMessage({ text: 'Kérés törölve.', isError: false });
    } catch (err) {
      setActionMessage({
        text: isApiError(err) ? err.message : 'Hiba a törlés során.',
        isError: true,
      });
    }
  };

  if (loading) {
    return <PageState>Kérések betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba történt: {error}</PageState>;
  }

  return (
    <main className='p-6 flex flex-col items-center gap-6 bg-white flex-1'>
      {actionMessage && (
        <span
          className={`w-full max-w-5xl text-lg font-medium ${
            actionMessage.isError ? 'text-red-500' : 'text-green-600'
          }`}
        >
          {actionMessage.text}
        </span>
      )}

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3 mb-2'>Bejövő kérések</h3>
        <IncomingRequestsContainer
          requests={requests.map(requestToRow)}
          onAccept={handleOpenAccept}
          onReject={(id) => void handleRejectRequest(id)}
          onEdit={handleOpenRequestEdit}
        />
      </div>

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3 mb-2'>Elfogadott kérések</h3>
        <ApprovedShiftsContainer
          shifts={acceptedRequests.map(requestToRow)}
          onEdit={(row) => handleOpenRequestEdit(row.id)}
          onDelete={(row) => void handleDeleteAcceptedRequest(row)}
        />
      </div>

      {acceptingRequest && (
        <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
          <div className='bg-white rounded-xl border-2 border-[#332C81] p-6 max-w-md w-full space-y-4 shadow-xl'>
            <h4 className='text-2xl font-bold text-[#332C81]'>Kérés elfogadása</h4>
            <p className='text-gray-600 font-medium'>{acceptingRequest.cookingClub?.name}</p>
            <p className='text-gray-600'>
              {formatLongDate(acceptingRequest.opening)} ·{' '}
              {formatTimeRange(acceptingRequest.opening, acceptingRequest.closing)}
            </p>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Műszakok száma:</label>
              <input
                type='number'
                min={1}
                max={4}
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={acceptShiftCount}
                onChange={(e) => setAcceptShiftCount(Number(e.target.value))}
              />
            </div>
            <div className='flex flex-col gap-1'>
              <label className='font-semibold text-[#332C81]'>Max. létszám:</label>
              <input
                type='number'
                min={1}
                max={6}
                className='border-2 border-gray-300 rounded-lg p-2 text-black'
                value={acceptMaxMembers}
                onChange={(e) => setAcceptMaxMembers(Number(e.target.value))}
              />
            </div>
            {actionMessage?.isError && <p className='text-red-500 font-medium'>{actionMessage.text}</p>}
            <div className='flex justify-end gap-3 pt-2'>
              <Button
                label='Mégse'
                variant='secondary'
                onClick={() => setAcceptingRequest(null)}
                disabled={isAccepting}
              />
              <Button
                label={isAccepting ? 'Elfogadás...' : 'Elfogadás'}
                variant='primary'
                onClick={() => void handleConfirmAccept()}
                disabled={isAccepting}
              />
            </div>
          </div>
        </div>
      )}

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
                <TimeInput value={editRequestStart} onChange={setEditRequestStart} />
              </div>
              <div className='flex flex-col gap-1 flex-1'>
                <label className='font-semibold text-[#332C81]'>Vége:</label>
                <TimeInput value={editRequestEnd} onChange={setEditRequestEnd} />
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
    </main>
  );
}
