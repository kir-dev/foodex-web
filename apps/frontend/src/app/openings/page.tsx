'use client';

import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { apiFetch, isApiError } from '@/lib/api';
import { formatLongDate, formatTime } from '@/lib/dates';
import { DetailedShiftDto } from '@/types/api';
import { useEffect, useState } from 'react';

export default function OpeningsPage() {
  return (
    <RequireAuth loadingLabel='Nyitások betöltése...'>
      <OpeningsContent />
    </RequireAuth>
  );
}

function OpeningsContent() {
  const [openings, setOpenings] = useState<DetailedShiftDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        const data = await apiFetch<DetailedShiftDto[]>('/api/openings');
        setOpenings(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült lekérni az elfogadott nyitásokat.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  if (loading) {
    return <PageState>Nyitások betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba: {error}</PageState>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center bg-white min-h-screen'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6'>
        <h1 className='text-3xl font-bold text-[#332C81] mb-6 pl-2'>Minden Elfogadott Nyitás (Múlt és Jövő)</h1>

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

                <div className='text-xs text-gray-400 text-right mt-4 border-t pt-2'>
                  Létszám: {shift.members.length + shift.newbies.length} / {shift.maxMembers}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}
