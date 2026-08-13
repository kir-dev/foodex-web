'use client';

import { useAuth } from '@/components/auth-provider';
import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { apiFetch, isApiError } from '@/lib/api';
import { DetailedUserDto, UpdateUserDto } from '@/types/api';
import { useEffect, useState } from 'react';

export default function ProfilePage() {
  return (
    <RequireAuth loadingLabel='Profil betöltése...'>
      <ProfileContent />
    </RequireAuth>
  );
}

function ProfileContent() {
  const { user, refresh } = useAuth();
  const [nickname, setNickname] = useState(user?.nickname || '');
  const [favouriteQuote, setFavouriteQuote] = useState(user?.favouriteQuote || '');
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<{ text: string; isError: boolean } | null>(null);

  useEffect(() => {
    setNickname(user?.nickname || '');
    setFavouriteQuote(user?.favouriteQuote || '');
  }, [user]);

  if (!user) {
    return <PageState>Profil betöltése...</PageState>;
  }

  const handleSave = async (): Promise<void> => {
    setIsSaving(true);
    setSaveMessage(null);

    try {
      const payload: UpdateUserDto = {
        nickname: nickname || null,
        favouriteQuote: favouriteQuote || null,
      };
      await apiFetch<DetailedUserDto>(`/api/users/${user.id}`, {
        method: 'PATCH',
        body: payload,
      });
      await refresh();
      setSaveMessage({ text: 'Változtatások sikeresen mentve!', isError: false });
    } catch (err) {
      setSaveMessage({
        text: isApiError(err) ? err.message : 'Hiba történt a mentés során.',
        isError: true,
      });
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className='w-full flex justify-center p-4 sm:p-6'>
      <div className='rounded-xl border-2 border-[#332C81] p-4 sm:p-8 w-full max-w-6xl space-y-6'>
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

          <div className='flex-1 bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4 flex flex-col justify-between'>
            <div>
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

              <div className='mb-2 text-xl font-semibold'>
                <span className='text-[#FF9860]'>E-mail:</span> <span className='text-white'>{user.email}</span>
              </div>

              <div className='mb-2 text-xl font-semibold'>
                <span className='text-[#FF9860]'>Jogosultság:</span>{' '}
                <span className='text-white'>{user.role.toLowerCase()}</span>
              </div>

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

        <div className='flex flex-col sm:flex-row sm:items-center gap-4 pt-2'>
          <Button
            label={isSaving ? 'Mentés...' : 'Profil mentése'}
            variant='primary'
            onClick={() => void handleSave()}
            disabled={isSaving}
          />

          {saveMessage && (
            <span className={`text-lg font-medium ${saveMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
              {saveMessage.text}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
