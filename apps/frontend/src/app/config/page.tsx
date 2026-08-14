'use client';

import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { RequireAuth } from '@/components/require-auth';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';
import { apiFetch, isApiError } from '@/lib/api';
import { toDateInputValue } from '@/lib/dates';
import { ConfigurationDto, isAdmin, UpdateConfigurationDto } from '@/types/api';
import { useEffect, useState } from 'react';

export default function ConfigPage() {
  return (
    <RequireAuth allow={isAdmin} loadingLabel='Konfiguráció betöltése...'>
      <ConfigContent />
    </RequireAuth>
  );
}

function ConfigContent() {
  const [feelingOfTheWeek, setFeelingOfTheWeek] = useState('');
  const [foodExLogo, setFoodExLogo] = useState('');
  const [homepageDescription, setHomepageDescription] = useState('');
  const [startOfSemester, setStartOfSemester] = useState('');
  const [endOfSemester, setEndOfSemester] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ text: string; isError: boolean } | null>(null);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        const config = await apiFetch<ConfigurationDto>('/api/config');
        setFeelingOfTheWeek(config.feelingOfTheWeek);
        setFoodExLogo(config.foodExLogo);
        setHomepageDescription(config.homepageDescription);
        setStartOfSemester(toDateInputValue(config.startOfSemester));
        setEndOfSemester(toDateInputValue(config.endOfSemester));
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a konfigurációt.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  const handleSave = async (): Promise<void> => {
    setMessage(null);
    if (
      !feelingOfTheWeek.trim() ||
      !foodExLogo.trim() ||
      !homepageDescription.trim() ||
      !startOfSemester ||
      !endOfSemester
    ) {
      setMessage({ text: 'Minden mező kitöltése kötelező.', isError: true });
      return;
    }
    if (startOfSemester > endOfSemester) {
      setMessage({ text: 'A félév kezdete nem lehet a vége után.', isError: true });
      return;
    }

    setSaving(true);
    try {
      const payload: UpdateConfigurationDto = {
        feelingOfTheWeek: feelingOfTheWeek.trim(),
        foodExLogo: foodExLogo.trim(),
        homepageDescription: homepageDescription.trim(),
        startOfSemester: `${startOfSemester}T00:00:00`,
        endOfSemester: `${endOfSemester}T23:59:59`,
      };
      const updated = await apiFetch<ConfigurationDto>('/api/config', {
        method: 'PATCH',
        body: payload,
      });
      setFeelingOfTheWeek(updated.feelingOfTheWeek);
      setFoodExLogo(updated.foodExLogo);
      setHomepageDescription(updated.homepageDescription);
      setStartOfSemester(toDateInputValue(updated.startOfSemester));
      setEndOfSemester(toDateInputValue(updated.endOfSemester));
      setMessage({ text: 'Konfiguráció mentve.', isError: false });
    } catch (err) {
      setMessage({
        text: isApiError(err) ? err.message : 'Nem sikerült menteni a konfigurációt.',
        isError: true,
      });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <PageState>Konfiguráció betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>{error}</PageState>;
  }

  return (
    <main className='p-4 sm:p-8 flex flex-col items-center bg-white min-h-screen'>
      <div className='w-full max-w-3xl border-2 border-[#332C81] rounded-2xl p-4 sm:p-8 space-y-5'>
        <h1 className='text-3xl font-bold text-[#332C81]'>Oldal konfiguráció</h1>
        <p className='text-[#332C81]'>
          A félév dátumai határozzák meg, mely műszakok jelennek meg a Nyitások oldalon. A feeling, a logó és a
          leírás a kezdőlapon látszik.
        </p>

        <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860] space-y-3'>
          <div>
            <StyledLabel>A hét feelingje</StyledLabel>
            <StyledInput type='text' value={feelingOfTheWeek} onChange={(e) => setFeelingOfTheWeek(e.target.value)} />
          </div>
          <div>
            <StyledLabel>FoodEx logó URL</StyledLabel>
            <StyledInput type='url' value={foodExLogo} onChange={(e) => setFoodExLogo(e.target.value)} />
          </div>
          <div>
            <StyledLabel>Kezdőlap leírása</StyledLabel>
            <textarea
              className='bg-white w-full p-3 rounded-2xl text-black text-xl h-32 mt-3'
              value={homepageDescription}
              onChange={(e) => setHomepageDescription(e.target.value)}
            />
          </div>
          <div className='flex flex-col sm:flex-row gap-4'>
            <div>
              <StyledLabel>Félév kezdete</StyledLabel>
              <StyledInput
                type='date'
                size='large'
                value={startOfSemester}
                onChange={(e) => setStartOfSemester(e.target.value)}
              />
            </div>
            <div>
              <StyledLabel>Félév vége</StyledLabel>
              <StyledInput
                type='date'
                size='large'
                value={endOfSemester}
                onChange={(e) => setEndOfSemester(e.target.value)}
              />
            </div>
          </div>
        </div>

        <div className='flex flex-col sm:flex-row sm:items-center gap-4'>
          <Button
            label={saving ? 'Mentés...' : 'Konfiguráció mentése'}
            variant='primary'
            onClick={() => void handleSave()}
            disabled={saving}
          />
          {message && (
            <span className={`text-lg font-medium ${message.isError ? 'text-red-500' : 'text-green-600'}`}>
              {message.text}
            </span>
          )}
        </div>
      </div>
    </main>
  );
}
