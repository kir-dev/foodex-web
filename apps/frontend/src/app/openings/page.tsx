'use client';

import { useEffect, useState } from 'react';

interface DetailedOpeningRequestDto {
  id: number;
  isAccepted: boolean;
  place: string;
  description: string;
  opening: string;
  closing: string;
  cookingClub: {
    id: number;
    name: string;
  };
  user: {
    id: number;
    name: string;
    nickname?: string;
  };
}

export default function OpeningsPage() {
  const [openings, setOpenings] = useState<DetailedOpeningRequestDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch('/api/accepted-requests', { credentials: 'include' })
      .then((res) => {
        if (!res.ok) {
          throw new Error('Nem sikerült lekérni az elfogadott nyitásokat.');
        }
        return res.json();
      })
      .then((data: DetailedOpeningRequestDto[]) => {
        if (Array.isArray(data)) {
          setOpenings(data);
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
    return <div className='p-8 text-center text-[#332C81] font-semibold'>Nyitások betöltése...</div>;
  }

  if (error) {
    return <div className='p-8 text-center text-red-500 font-semibold'>Hiba: {error}</div>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center bg-white min-h-screen'>
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-6'>
        <h1 className='text-3xl font-bold text-[#332C81] mb-6 pl-2'>Minden Elfogadott Nyitás (Múlt és Jövő)</h1>

        {openings.length === 0 ? (
          <p className='text-gray-500 text-lg pl-2'>Nincsenek elfogadott nyitások az adatbázisban.</p>
        ) : (
          <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
            {openings.map((req) => {
              const start = new Date(req.opening);
              const end = new Date(req.closing);

              return (
                <div
                  key={req.id}
                  className='border-2 border-[#332C81] bg-white rounded-xl p-4 flex flex-col justify-between hover:shadow-md transition-shadow'
                >
                  <div>
                    <div className='flex justify-between items-start mb-2'>
                      <h3 className='text-2xl font-bold text-[#332C81]'>{req.cookingClub?.name || `Kör #${req.id}`}</h3>
                      <span className='bg-green-100 text-green-800 text-xs font-semibold px-2.5 py-0.5 rounded-full'>
                        Elfogadva
                      </span>
                    </div>

                    <p className='text-gray-700 font-medium mb-1'>
                      📅{' '}
                      {start.toLocaleDateString('hu-HU', {
                        weekday: 'long',
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                      })}
                    </p>
                    <p className='text-gray-600 mb-1'>
                      🕒 {start.toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })} -{' '}
                      {end.toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })}
                    </p>
                    <p className='text-gray-600 mb-2'>
                      📍 Helyszín: <span className='font-semibold text-black'>{req.place}</span>
                    </p>

                    {req.description && (
                      <p className='text-sm text-gray-500 italic border-t pt-2 mt-2'>"{req.description}"</p>
                    )}
                  </div>

                  <div className='text-xs text-gray-400 text-right mt-4 border-t pt-2'>
                    Szervező: {req.user?.nickname || req.user?.name || 'Ismeretlen'}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </main>
  );
}
