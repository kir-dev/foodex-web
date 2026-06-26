'use client';

import Button from '@/components/button';
import { useEffect, useState } from 'react';

type Role = 'ADMIN' | 'MEMBER' | 'NEWBIE' | 'GUEST';

type DetailedUserDto = {
  id: number;
  role: Role;
  name: string;
  nickname: string | null;
  email: string;
  favouriteQuote: string | null;
  isActive: boolean;
  profilePicture: string | null;
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
    fetch('/backend-api/homepage', { credentials: 'include' })
      .then((res) => {
        if (res.status === 401 || res.status === 403) {
          window.location.href = 'http://localhost:8080/oauth2/authorization/authsch';
          return null;
        }
        if (!res.ok) throw new Error('Bejelentkezési ellenőrzés sikertelen.');
        return res.json();
      })
      .then((homeData) => {
        if (!homeData) return null;
        return fetch('/backend-api/users/1', { credentials: 'include' });
      })
      .then((res) => {
        if (!res) return null;
        if (!res.ok) throw new Error('Nem sikerült lekérni a felhasználó profilját.');
        return res.json();
      })
      .then((userData: DetailedUserDto | null) => {
        if (userData) {
          setUser(userData);
          setNickname(userData.nickname || '');
          setFavouriteQuote(userData.favouriteQuote || '');
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  const handleSave = async () => {
    if (!user) return;

    setIsSaving(true);
    setSaveMessage(null);

    try {
      // JSON helyett Query stringet építünk fel
      const params = new URLSearchParams();
      params.append('name', user.name);
      if (nickname) params.append('nickname', nickname);
      params.append('email', user.email);
      if (favouriteQuote) params.append('favouriteQuote', favouriteQuote);
      if (user.profilePicture) params.append('profilePicture', user.profilePicture);

      // Az URL végére fűzzük a paramétereket, mert a Spring így fogja tudni beolvasni a hibás import miatt
      const res = await fetch(`/backend-api/users/${user.id}?${params.toString()}`, {
        method: 'PATCH',
        headers: {
          Accept: 'application/json',
        },
      });

      if (!res.ok) {
        throw new Error('Sikertelen mentés a szerveren.');
      }

      const updatedUser = await res.json();
      setUser(updatedUser);
      setNickname(updatedUser.nickname || '');
      setFavouriteQuote(updatedUser.favouriteQuote || '');
      setSaveMessage({ text: 'Változtatások sikeresen mentve!', isError: false });
    } catch (err: any) {
      console.error(err);
      setSaveMessage({ text: err.message || 'Hiba történt a mentés során.', isError: true });
    } finally {
      setIsSaving(false);
    }
  };

  if (loading) {
    return (
      <div className='w-full min-h-screen flex items-center justify-center bg-white text-xl font-semibold text-[#332C81]'>
        Profil ellenőrzése és betöltése...
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
        {/* Felső rész */}
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

          {/* Jobb oldali infó */}
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

              {/* Idézet */}
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

        {/* Alsó rész */}
        <div className='bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4'>
          <h2 className='text-[#FF9860] font-semibold mb-2 text-2xl tracking-wide'>Féléves tevékenységek</h2>
          <div className='h-40 bg-[#332C81]' />
        </div>

        {/* Mentés szekció az új Button komponenssel */}
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
