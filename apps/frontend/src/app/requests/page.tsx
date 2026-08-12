'use client';

import { ApprovedShiftsContainer } from '@/components/approvedShiftsContainer';
import Button from '@/components/button';
import { IncomingRequestsContainer } from '@/components/incomingRequestsContainer';
import { DetailedOpeningRequestDto } from '@/types/requestPageData';
import { useEffect, useState } from 'react';

const BACKEND_URL = 'http://localhost:8080';

function getCookie(name: string): string | null {
  if (typeof document === 'undefined') return null;
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
  return null;
}

function getCalculatedShiftsCount(openingStr: string, closingStr: string, defaultShiftHours = 2): number {
  const start = new Date(openingStr).getTime();
  const end = new Date(closingStr).getTime();
  const totalHours = (end - start) / (1000 * 60 * 60);
  const count = Math.round(totalHours / defaultShiftHours);
  return Math.max(1, count);
}

export interface DetailedShiftDto {
  id: number;
  opening: string;
  closing: string;
  place: string;
  comment?: string;
  maxMembers: number;
  cookingClub: {
    id: number;
    name: string;
  };
  members?: any[];
  newbies?: any[];
}

export default function RequestsPage() {
  const [requests, setRequests] = useState<DetailedOpeningRequestDto[] | null>(null);
  const [acceptedShifts, setAcceptedShifts] = useState<DetailedShiftDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Szerkesztéshez szükséges state-ek
  const [editingShift, setEditingShift] = useState<DetailedShiftDto | null>(null);
  const [editMaxMembers, setEditMaxMembers] = useState<number>(20);
  const [editPlace, setEditPlace] = useState<string>('');
  const [editComment, setEditComment] = useState<string>('');
  const [isSavingShift, setIsSavingShift] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // 1. Bejövő kérések lekérése
        const reqRes = await fetch(`${BACKEND_URL}/api/incoming-requests`, {
          method: 'GET',
          credentials: 'include',
          headers: { Accept: 'application/json' },
        });

        if (reqRes.status === 401 || reqRes.status === 403) {
          window.location.href = `${BACKEND_URL}/oauth2/authorization/authsch`;
          return;
        }

        if (!reqRes.ok) throw new Error('Nem sikerült betölteni a bejövő kéréseket.');
        const requestsData: DetailedOpeningRequestDto[] = await reqRes.json();
        setRequests(requestsData);

        // 2. Már elfogadott/aktív műszakok lekérése a backendről
        const shiftRes = await fetch(`${BACKEND_URL}/api/shifts`, {
          method: 'GET',
          credentials: 'include',
          headers: { Accept: 'application/json' },
        });

        if (shiftRes.ok) {
          const shiftsData = await shiftRes.json();
          setAcceptedShifts(Array.isArray(shiftsData) ? shiftsData : []);
        }
      } catch (err: any) {
        console.error(err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleAcceptRequest = async (requestId: number) => {
    try {
      const targetRequest = requests?.find((r) => r.id === requestId);
      const numberOfShifts = targetRequest
        ? getCalculatedShiftsCount(targetRequest.opening, targetRequest.closing, 2)
        : 1;

      let xsrfToken = getCookie('XSRF-TOKEN');
      if (!xsrfToken) {
        await fetch(`${BACKEND_URL}/api/homepage`, { credentials: 'include' });
        xsrfToken = getCookie('XSRF-TOKEN');
      }

      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      };

      if (xsrfToken) {
        headers['X-XSRF-TOKEN'] = xsrfToken;
      }

      const response = await fetch(`${BACKEND_URL}/api/requests/${requestId}`, {
        method: 'POST',
        headers,
        credentials: 'include',
        body: JSON.stringify({
          maxMembers: 20,
          numberOfShifts: numberOfShifts,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Szerver hiba (${response.status}): ${errorText || 'Ismeretlen hiba'}`);
      }

      const newShifts: DetailedShiftDto[] = await response.json();

      setRequests((prev) => (prev ? prev.filter((req) => req.id !== requestId) : null));
      setAcceptedShifts((prev) => [...prev, ...(Array.isArray(newShifts) ? newShifts : [])]);
    } catch (err: any) {
      console.error('Sikertelen kérés részletei:', err);
      alert(`Nem sikerült a kérés: ${err.message}`);
    }
  };

  // Módosítás gomb megnyitása
  const handleOpenEditModal = (shift: any) => {
    const fullShift = acceptedShifts.find((s) => s.id === shift.id);
    if (!fullShift) return;

    setEditingShift(fullShift);
    setEditMaxMembers(fullShift.maxMembers || 20);
    setEditPlace(fullShift.place || '');
    setEditComment(fullShift.comment || '');
  };

  // Műszak mentése (PATCH /api/shifts/{shiftId})
  const handleSaveShift = async () => {
    if (!editingShift) return;

    setIsSavingShift(true);
    try {
      let xsrfToken = getCookie('XSRF-TOKEN');
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      };

      if (xsrfToken) {
        headers['X-XSRF-TOKEN'] = xsrfToken;
      }

      const response = await fetch(`${BACKEND_URL}/api/shifts/${editingShift.id}`, {
        method: 'PATCH',
        headers,
        credentials: 'include',
        body: JSON.stringify({
          maxMembers: editMaxMembers,
          place: editPlace,
          comment: editComment,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Szerver hiba (${response.status}): ${errorText || 'Ismeretlen hiba'}`);
      }

      const updatedShift: DetailedShiftDto = await response.json();

      setAcceptedShifts((prev) => prev.map((s) => (s.id === updatedShift.id ? updatedShift : s)));
      setEditingShift(null);
    } catch (err: any) {
      console.error('Műszak mentési hiba:', err);
      alert(`Hiba a mentés során: ${err.message}`);
    } finally {
      setIsSavingShift(false);
    }
  };

  // Műszak törlése (DELETE /api/shifts/{shiftId})
  const handleDeleteShift = async (shift: any) => {
    if (!confirm('Biztosan törölni szeretnéd ezt a műszakot?')) return;

    try {
      let xsrfToken = getCookie('XSRF-TOKEN');
      const headers: Record<string, string> = {
        Accept: 'application/json',
      };

      if (xsrfToken) {
        headers['X-XSRF-TOKEN'] = xsrfToken;
      }

      const response = await fetch(`${BACKEND_URL}/api/shifts/${shift.id}`, {
        method: 'DELETE',
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Szerver hiba (${response.status}): ${errorText || 'Ismeretlen hiba'}`);
      }

      // Eltávolítjuk a state-ből
      setAcceptedShifts((prev) => prev.filter((s) => s.id !== shift.id));
    } catch (err: any) {
      console.error('Műszak törlési hiba:', err);
      alert(`Hiba a törlés során: ${err.message}`);
    }
  };

  if (loading) {
    return (
      <div className='w-full min-h-screen flex items-center justify-center bg-white text-xl font-semibold text-[#332C81]'>
        Kérések és műszakok betöltése...
      </div>
    );
  }

  if (error) {
    return (
      <div className='w-full min-h-screen flex items-center justify-center bg-white text-xl font-semibold text-red-500'>
        Hiba történt: {error}
      </div>
    );
  }

  if (!requests) return null;

  return (
    <main className='p-6 flex flex-col items-center gap-6 bg-white min-h-screen'>
      {/* Bejövő kérések táblázata */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3 mb-2'>Bejövő kérések</h3>
        <IncomingRequestsContainer
          requests={(requests || []).map((req) => ({
            id: req.id,
            groupName: req.cookingClub?.name || `Kör ID: ${req.cookingClub?.id}`,
            day: new Date(req.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: new Date(req.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
            location: req.place,
            date: new Date(req.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
          }))}
          onAccept={handleAcceptRequest}
        />
      </div>

      {/* Elfogadott műszakok táblázata */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3 mb-2'>Elfogadott műszakok</h3>
        <ApprovedShiftsContainer
          shifts={(Array.isArray(acceptedShifts) ? acceptedShifts : []).map((shift) => ({
            id: shift.id,
            groupName: shift.cookingClub?.name || `Kör #${shift.cookingClub?.id}`,
            day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: `${new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })} - ${new Date(shift.closing).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })}`,
            location: shift.place,
            date: new Date(shift.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
            onEdit: () => handleOpenEditModal(shift),
            onDelete: () => handleDeleteShift(shift),
          }))}
        />
      </div>

      {/* Szerkesztés Modal (Csak Mentés gombbal lezárható) */}
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
                label={isSavingShift ? 'Mentés...' : 'Mentés'}
                variant='primary'
                onClick={handleSaveShift}
                disabled={isSavingShift}
              />
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
