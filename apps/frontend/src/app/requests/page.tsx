'use client';

import { ApprovedShiftsContainer } from '@/components/approvedShiftsContainer';
import { IncomingRequestsContainer } from '@/components/incomingRequestsContainer';
import { DetailedOpeningRequestDto } from '@/types/requestPageData';
import { useEffect, useState } from 'react';

interface DetailedShiftDto {
  id: number;
  opening: string;
  closing: string;
  place: string;
  comment: string;
  maxMembers: number;
  cookingClub: {
    id: number;
    name: string;
  };
  members: any[];
  newbies: any[];
}

export default function RequestsPage() {
  const [requests, setRequests] = useState<DetailedOpeningRequestDto[] | null>(null);
  const [acceptedShifts, setAcceptedShifts] = useState<DetailedShiftDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // A query paraméterben megmondjuk a proxynak, mit kérünk a Spring-től
    fetch('/api/foodex-proxy?endpoint=incoming-requests')
      .then((res) => {
        if (res.status === 401 || res.status === 403) {
          window.location.href = 'http://localhost:8080/oauth2/authorization/authsch';
          return null;
        }
        if (!res.ok) throw new Error('Nem sikerült betölteni a bejövő kéréseket.');
        return res.json();
      })
      .then((requestsData: DetailedOpeningRequestDto[] | null) => {
        if (requestsData) {
          setRequests(requestsData);
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  const handleAcceptRequest = async (requestId: number) => {
    try {
      // Az endpoint paraméterben adjuk át a pontos backend URL-t
      const response = await fetch(`/api/foodex-proxy?endpoint=requests/${requestId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          maxMembers: 20,
          comment: '',
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Szerver hiba (${response.status}): ${errorText || 'Ismeretlen hiba'}`);
      }

      const newShifts: DetailedShiftDto[] = await response.json();

      setRequests((prev) => (prev ? prev.filter((req) => req.id !== requestId) : null));
      setAcceptedShifts((prev) => [...prev, ...newShifts]);
    } catch (err: any) {
      console.error('Sikertelen kérés részletei:', err);
      alert(`Nem sikerült a kérés: ${err.message}`);
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
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Bejövő kérések</h3>
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

      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Elfogadott műszakok</h3>
        <ApprovedShiftsContainer
          shifts={acceptedShifts.map((shift) => ({
            groupName: shift.cookingClub?.name || `Kör #${shift.cookingClub?.id}`,
            day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: `${new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })} - ${new Date(shift.closing).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })}`,
            location: shift.place,
            date: new Date(shift.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
          }))}
        />
      </div>
    </main>
  );
}
