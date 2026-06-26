'use client';

import Button from '@/components/button';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';
import { useState } from 'react';

const COOKING_CLUB_IDS: Record<string, number> = {
  pizzásch: 223,
  americano: 403,
  vödör: 179,
  lángosch: 473,
  kakas: 31,
  paschta: 528,
  palacsintázó: 395,
  reggelisch: 490,
  dobozosch: 529,
};

export default function RequestingPage() {
  const [cookingClubName, setCookingClubName] = useState('');
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [location, setLocation] = useState('');
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!cookingClubName || !date || !startTime || !endTime || !location) {
      alert('Kérlek tölts ki minden kötelező mezőt!');
      return;
    }

    const clubId = COOKING_CLUB_IDS[cookingClubName.toLowerCase().trim()];
    if (!clubId) {
      alert('Ismeretlen kör név! Kérlek a listából válassz (pl. Pizzásch, Vödör...)');
      return;
    }

    setLoading(true);

    try {
      const openingDateTime = `${date}T${startTime}:00`;
      const closingDateTime = `${date}T${endTime}:00`;

      const payload = {
        cookingClubId: clubId,
        opening: openingDateTime,
        closing: closingDateTime,
        place: location,
        comment: comment || null,
      };

      // Javított útvonal: /backend-api/requests a /backend-api/incoming-requests helyett!
      const response = await fetch('/backend-api/requests', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        alert('Kérés sikeresen elküldve!');
        setCookingClubName('');
        setDate('');
        setStartTime('');
        setEndTime('');
        setLocation('');
        setComment('');
      } else {
        const errorData = await response.text();
        alert(`Hiba történt a küldés során: ${response.status} - ${errorData}`);
      }
    } catch (error) {
      console.error('Hálózati hiba:', error);
      alert('Nem sikerült elérni a backend szervert.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className='px-4 sm:px-8 py-8 flex flex-col items-center'>
      <div className='w-full max-w-[1280px] border-2 border-[#332C81] rounded-2xl p-4 sm:p-8'>
        <div className='flex flex-col md:flex-row gap-4 md:gap-6 pb-5 w-full'>
          <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860] w-full md:w-1/4'>
            <StyledLabel>Kör neve</StyledLabel>
            <StyledInput
              type='text'
              placeholder='pl. Pizzásch, Vödör, Kakas...'
              size='full'
              value={cookingClubName}
              onChange={(e) => setCookingClubName(e.target.value)}
            />
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

        <div className='flex flex-col sm:flex-row justify-between items-center gap-4 sm:gap-0 w-full'>
          <div className='flex flex-col sm:flex-row gap-4 sm:gap-4 w-full sm:w-auto'>
            <Button
              label='Adatok betöltése'
              variant='secondary'
              onClick={() => alert('Ez a funkció még fejlesztés alatt áll!')}
            />
            <Button
              label='Adatok mentése'
              variant='secondary'
              onClick={() => alert('Ez a funkció még fejlesztés alatt áll!')}
            />
          </div>
          <Button
            label={loading ? 'Küldés...' : 'Kérés leadása'}
            variant='primary'
            onClick={handleSubmit}
            disabled={loading}
          />
        </div>
      </div>
    </div>
  );
}
