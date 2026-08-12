'use client';

import Button from '@/components/button';
import { useEffect, useState } from 'react';

const BACKEND_URL = 'http://localhost:8080';

export type Role = 'ADMIN' | 'MEMBER' | 'NEWBIE' | 'GUEST';

export type DetailedUserDto = {
  id: number;
  role: Role;
  name: string;
  nickname: string | null;
  email: string;
  favouriteQuote: string | null;
  isActive: boolean;
  profilePicture: string | null;
  leaderAt?: any[];
  shifts?: any[];
  requests?: any[];
};

export default function ProfilePage() {
  const [user, setUser] = useState<DetailedUserDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [nickname, setNickname] = useState('');
  const [favouriteQuote, setFavouriteQuote] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<{ text: string; isError: boolean } | null>(null);

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        // 1. Kiolvassuk a localStorage-ban tárolt userId-t
        const storedUserId = localStorage.getItem('userId');

        if (!storedUserId) {
          throw new Error('Nincs elmentett azonosító. Kérlek, nyisd meg először a főoldalt!');
        }

        // 2. Lekérjük a konkrét usert: GET /api/users/{userId}
        const res = await fetch(`${BACKEND_URL}/api/users/${storedUserId}`, {
          method: 'GET',
          credentials: 'include',
          headers: { Accept: 'application/json' },
        });

        if (res.status === 401 || res.status === 403) {
          window.location.href = `${BACKEND_URL}/oauth2/authorization/authsch`;
          return;
        }

        if (!res.ok) {
          throw new Error(`Nem sikerült betölteni a profil adatokat (HTTP ${res.status}).`);
        }

        const userData: DetailedUserDto = await res.json();
        setUser(userData);
        setNickname(userData.nickname || '');
        setFavouriteQuote(userData.favouriteQuote || '');
      } catch (err: any) {
        console.error('Profil betöltési hiba:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, []);

  // Segédfüggvény a süti kiolvasásához (ha még nincs a fájl tetején)
  function getCookie(name: string): string | null {
    if (typeof document === 'undefined') return null;
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop()?.split(';').shift() || null;
    return null;
  }

  const handleSave = async () => {
    if (!user) return;

    setIsSaving(true);
    setSaveMessage(null);

    try {
      // 1. CSRF token beszerzése
      let xsrfToken = getCookie('XSRF-TOKEN');
      if (!xsrfToken) {
        // Ha nincs süti, lekérünk egy védett oldalt/végpontot, ami beállítja a CSRF sütit
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

      const response = await fetch(`${BACKEND_URL}/api/users/${user.id}`, {
        method: 'PATCH',
        credentials: 'include',
        headers,
        body: JSON.stringify({
          name: user.name,
          nickname: nickname || null,
          email: user.email,
          favouriteQuote: favouriteQuote || null,
          profilePicture: user.profilePicture || null,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Szerver hiba (${response.status}): ${errorText || 'Sikertelen mentés'}`);
      }

      const updatedUser: DetailedUserDto = await response.json();
      setUser(updatedUser);
      setNickname(updatedUser.nickname || '');
      setFavouriteQuote(updatedUser.favouriteQuote || '');
      setSaveMessage({ text: 'Változtatások sikeresen mentve!', isError: false });
    } catch (err: any) {
      console.error('Mentési hiba:', err);
      setSaveMessage({ text: err.message || 'Hiba történt a mentés során.', isError: true });
    } finally {
      setIsSaving(false);
    }
  };

  if (loading) {
    return (
      <div className='w-full min-h-screen flex items-center justify-center bg-white text-xl font-semibold text-[#332C81]'>
        Profil betöltése...
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

  if (!user) return null;

  return (
    <div className='w-full flex justify-center p-4 sm:p-6'>
      <div className='rounded-xl border-2 border-[#332C81] p-4 sm:p-8 w-full max-w-6xl space-y-6'>
        {/* Felső rész: Profil adatok */}
        <div className='flex flex-col md:flex-row gap-6'>
          {user.profilePicture ? (
            <img
              src={user.profilePicture}
              alt='Profilkép'
              className='w-48 h-48 sm:w-60 sm:h-60 md:w-72 md:h-72 rounded-xl mx-auto md:mx-0 border-2 border-[#FF9860] object-cover'
            />
          ) : (
            <div className='w-48 h-48 sm:w-60 sm:h-60 md:w-72 md:h-72 bg-gray-300 rounded-xl mx-auto md:mx-0 border-2 border-[#FF9860]' />
          )}

          {/* Jobb oldali infó block */}
          <div className='flex-1 bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4 flex flex-col justify-between'>
            <div>
              {/* Név + Becenév */}
              <div className='mb-4 flex flex-col gap-2 md:flex-row md:items-center md:gap-4'>
                <div className='flex items-center gap-2 text-xl font-semibold'>
                  <span className='text-[#FF9860]'>Név:</span>
                  <span className='text-white'>{user.name}</span>
                </div>

                <div className='flex flex-col md:flex-row md:items-center gap-2 w-full md:w-auto'>
                  <span className='text-[#FF9860] font-semibold text-xl'>Becenév:</span>
                  <input
                    type='text'
                    className='rounded-xl px-2 py-1 w-full md:w-48 bg-white text-black placeholder-gray-300'
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    placeholder='Add meg a beceneved'
                  />
                </div>
              </div>

              {/* Email */}
              <div className='mb-2 text-xl font-semibold'>
                <span className='text-[#FF9860]'>E-mail:</span> <span className='text-white'>{user.email}</span>
              </div>

              {/* Jogosultság */}
              <div className='mb-2 text-xl font-semibold'>
                <span className='text-[#FF9860]'>Jogosultság:</span>{' '}
                <span className='text-white'>{user.role.toLowerCase()}</span>
              </div>

              {/* Kedvenc Idézet */}
              <div className='mb-4'>
                <span className='text-[#FF9860] font-semibold text-xl'>Kedvenc idézet</span>
                <textarea
                  className='w-full h-20 rounded-xl px-2 py-1 mt-3 bg-white text-black placeholder-gray-300'
                  value={favouriteQuote}
                  onChange={(e) => setFavouriteQuote(e.target.value)}
                  placeholder='Írd ide az idézeted'
                />
              </div>
            </div>
          </div>
        </div>

        {/* Alsó rész: Féléves tevékenységek */}
        <div className='bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4'>
          <h2 className='text-[#FF9860] font-semibold mb-2 text-2xl tracking-wide'>Féléves tevékenységek</h2>
          <div className='text-white'>
            {user.shifts && user.shifts.length > 0 ? (
              <p className='text-lg'>Ledolgozott műszakok száma: {user.shifts.length}</p>
            ) : (
              <p className='text-gray-300 italic'>Nincsenek még műszakjaid ebben a félévben.</p>
            )}
          </div>
        </div>

        {/* Mentés gomb és visszajelző üzenet */}
        <div className='flex flex-col sm:flex-row sm:items-center gap-4 pt-2'>
          <Button
            label={isSaving ? 'Mentés...' : 'Profil mentése'}
            variant='primary'
            onClick={handleSave}
            disabled={isSaving}
          />

          {saveMessage && (
            <span className={`text-lg font-medium ${saveMessage.isError ? 'text-red-400' : 'text-green-400'}`}>
              {saveMessage.text}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
