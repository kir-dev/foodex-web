'use client';

import { ActiveShiftsContainer } from '@/components/activeShiftsContainer';
import { SubmitShiftsContainer } from '@/components/submitShiftsContainer';
import { ShiftsPageData } from '@/types/shiftsPageData';
import { useEffect, useState } from 'react';

export default function ShiftsPage() {
  const [data, setData] = useState<ShiftsPageData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch('/backend-api/shifts', { credentials: 'include' })
      .then((res) => {
        if (res.status === 401 || res.status === 403) {
          window.location.href = 'http://localhost:8080/oauth2/authorization/authsch';
          return null;
        }
        if (!res.ok) throw new Error('Nem sikerült betölteni a műszakok adatait.');
        return res.json();
      })
      .then((shiftsData: ShiftsPageData | null) => {
        if (shiftsData) {
          setData(shiftsData);
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
        Műszakok betöltése...
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
      {/* Aktív műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Aktív műszakok</h3>
        <ActiveShiftsContainer
          shifts={(data.activeShifts || []).map((shift) => ({
            // A shift.cookingClubId helyett a beágyazott objektum nevét jelenítjük meg
            groupName: shift.cookingClub?.name || `Kör #${shift.cookingClub?.id}`,
            day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
            location: shift.place,
            date: new Date(shift.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
          }))}
        />
      </div>

      {/* Betelt műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Betelt műszakok</h3>
        <SubmitShiftsContainer
          shifts={(data.fullShifts || []).map((shift) => ({
            // Ugyanaz az objektum-alapú névkezelés itt is
            groupName: shift.cookingClub?.name || `Kör #${shift.cookingClub?.id}`,
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
