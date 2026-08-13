'use client';

import { useAuth } from '@/components/auth-provider';
import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';
import { apiFetch, isApiError } from '@/lib/api';
import { toLocalDateTimePayload } from '@/lib/dates';
import {
  CookingClubDto,
  CreateOpeningRequestDto,
  DetailedCookingClubDto,
  DetailedOpeningRequestDto,
  isClubLeaderOrAdmin,
} from '@/types/api';
import { useEffect, useMemo, useState } from 'react';

export default function RequestingPage() {
  return (
    <RequireAuth allow={isClubLeaderOrAdmin} loadingLabel='Űrlap betöltése...'>
      <RequestingContent />
    </RequireAuth>
  );
}

function RequestingContent() {
  const { user } = useAuth();
  const [clubs, setClubs] = useState<CookingClubDto[]>([]);
  const [clubsError, setClubsError] = useState<string | null>(null);
  const [cookingClubId, setCookingClubId] = useState<number | ''>('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [location, setLocation] = useState('');
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);
  const [formMessage, setFormMessage] = useState<{ text: string; isError: boolean } | null>(null);

  const selectableClubs = useMemo(() => {
    if (!user) {
      return clubs;
    }
    if (user.role === 'ADMIN') {
      return clubs;
    }
    return user.leaderAt;
  }, [clubs, user]);

  useEffect(() => {
    const loadClubs = async (): Promise<void> => {
      try {
        const data = await apiFetch<DetailedCookingClubDto[]>('/api/cooking-clubs');
        setClubs(Array.isArray(data) ? data.map((club) => ({ id: club.id, name: club.name })) : []);
      } catch (err) {
        setClubsError(isApiError(err) ? err.message : 'Nem sikerült betölteni a köröket.');
      }
    };

    void loadClubs();
  }, []);

  if (!user) {
    return <PageState>Űrlap betöltése...</PageState>;
  }

  const handleSubmit = async (): Promise<void> => {
    setFormMessage(null);

    if (cookingClubId === '' || !date || !startTime || !endTime || !location) {
      setFormMessage({ text: 'Kérlek tölts ki minden kötelező mezőt!', isError: true });
      return;
    }

    setLoading(true);
    try {
      const payload: CreateOpeningRequestDto = {
        cookingClubId: Number(cookingClubId),
        opening: toLocalDateTimePayload(date, startTime),
        closing: toLocalDateTimePayload(date, endTime),
        place: location.trim(),
        description: comment.trim(),
      };

      await apiFetch<DetailedOpeningRequestDto>('/api/requests', {
        method: 'POST',
        body: payload,
      });

      setFormMessage({ text: 'Kérés sikeresen elküldve!', isError: false });
      setCookingClubId('');
      setDate('');
      setStartTime('');
      setEndTime('');
      setLocation('');
      setComment('');
    } catch (error) {
      setFormMessage({
        text: isApiError(error) ? error.message : 'Hálózati hiba történt.',
        isError: true,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className='px-4 sm:px-8 py-8 flex flex-col items-center'>
      <div className='w-full max-w-[1280px] border-2 border-[#332C81] rounded-2xl p-4 sm:p-8'>
        {clubsError && <p className='mb-4 text-red-500 font-semibold'>{clubsError}</p>}

        <div className='flex flex-col md:flex-row gap-4 md:gap-6 pb-5 w-full'>
          <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860] w-full md:w-1/4'>
            <StyledLabel>Kör neve</StyledLabel>
            <select
              className='bg-white p-2 rounded-2xl text-black text-xl mt-2 w-full'
              value={cookingClubId}
              onChange={(e) => setCookingClubId(e.target.value ? Number(e.target.value) : '')}
            >
              <option value=''>Válassz kört</option>
              {selectableClubs.map((club) => (
                <option key={club.id} value={club.id}>
                  {club.name}
                </option>
              ))}
            </select>
          </div>

          <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860] flex-1 md:w-3/4'>
            <StyledLabel>Nyitás</StyledLabel>
            <div className='flex flex-col sm:flex-row sm:flex-wrap gap-4 sm:gap-6 items-start sm:items-center w-full'>
              <div className='flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3.5 w-full sm:w-auto'>
                <StyledLabel>Napja:</StyledLabel>
                <StyledInput type='date' size='large' value={date} onChange={(e) => setDate(e.target.value)} />
              </div>

              <div className='flex flex-col sm:flex-row sm:items-center gap-2 w-full sm:w-auto'>
                <StyledLabel>Ideje:</StyledLabel>
                <div className='flex items-center gap-2 w-full sm:w-auto text-black'>
                  <input
                    type='time'
                    className='rounded-2xl p-2 text-xl bg-white w-full sm:w-auto'
                    step={900}
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                  />
                  <span className='mx-1 text-[#ff9860] font-semibold mt-2'>–</span>
                  <input
                    type='time'
                    className='rounded-2xl p-2 text-xl bg-white w-full sm:w-auto'
                    step={900}
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                  />
                </div>
              </div>

              <div className='flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3.5 w-full sm:w-auto'>
                <StyledLabel>Helye:</StyledLabel>
                <StyledInput
                  type='text'
                  placeholder='pl. 13. konyha'
                  size='medium'
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>

        <div className='bg-[#2f2173] text-white p-4 rounded-2xl border-2 border-[#ff9860] mb-5 w-full'>
          <StyledLabel>Megjegyzés</StyledLabel>
          <textarea
            placeholder='pl. különleges nyitás, szokásosnál több foodexes kell, stb... (max 200 karakter lehet)'
            maxLength={200}
            className='bg-white w-full p-3 rounded-2xl text-black text-xl h-32 mt-4'
            value={comment}
            onChange={(e) => setComment(e.target.value)}
          />
        </div>

        <div className='flex flex-col sm:flex-row justify-between items-center gap-4 w-full'>
          <Button
            label={loading ? 'Küldés...' : 'Kérés leadása'}
            variant='primary'
            onClick={() => void handleSubmit()}
            disabled={loading}
          />
          {formMessage && (
            <span className={`text-lg font-medium ${formMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
              {formMessage.text}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
