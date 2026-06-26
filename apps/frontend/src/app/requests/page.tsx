'use client';

import { ApprovedShiftsContainer } from '@/components/approvedShiftsContainer';
import { IncomingRequestsContainer } from '@/components/incomingRequestsContainer';
import { RequestPageData } from '@/types/requestPageData';
import { useEffect, useState } from 'react';

export default function RequestsPage() {
  const [data, setData] = useState<RequestPageData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch('/backend-api/incoming-requests', { credentials: 'include' })
      .then((res) => {
        if (res.status === 401 || res.status === 403) {
          window.location.href = 'http://localhost:8080/oauth2/authorization/authsch';
          return null;
        }
        if (!res.ok) throw new Error('Nem sikerült betölteni a bejövő kéréseket.');
        return res.json();
      })
      .then((requestData: RequestPageData | null) => {
        if (requestData) {
          setData(requestData);
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

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

  if (!data) return null;

  return (
    <main className='p-6 flex flex-col items-center gap-6 bg-white min-h-screen'>
      {/* Bejövő kérések */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Bejövő kérések</h3>
        <IncomingRequestsContainer
          // A ?. és || [] gondoskodik róla, hogy ha nincs adat, egy üres listát kapjon a komponens, és ne szálljon el a felület
          requests={(data.incomingFoodExRequests || []).map((req) => ({
            groupName: `Kör ID: ${req.cookingClubId}`,
            day: new Date(req.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: new Date(req.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
            location: req.place,
            date: new Date(req.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
          }))}
        />
      </div>

      {/* Elfogadott műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Elfogadott műszakok</h3>
        <ApprovedShiftsContainer
          // Ugyanaz a védelem itt is
          shifts={(data.acceptedShifts || []).map((shift) => ({
            groupName: `Kör #${shift.cookingClubId}`,
            day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
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
