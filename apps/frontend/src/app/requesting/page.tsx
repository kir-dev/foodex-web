'use client';

import Button from '@/components/button';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';

export default function RequestingPage() {
  return (
    <div className='px-4 sm:px-8 py-8 flex flex-col items-center'>
      <div className='w-full max-w-[1280px] border-2 border-[#332C81] rounded-2xl p-4 sm:p-8'>
        {/* Kör neve + Nyitás */}
        <div className='flex flex-col md:flex-row gap-4 md:gap-6 pb-5 w-full'>
          {/* Kör neve */}
          <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860] w-full md:w-1/4'>
            <StyledLabel>Kör neve</StyledLabel>
            <StyledInput type='text' placeholder='pl. Pizzásch, Vödör, Kakas...' size='full' />
          </div>

          {/* Nyitás */}
          <div className='bg-[#332C81] text-white p-4 rounded-2xl border-2 border-[#ff9860] flex-1 md:w-3/4'>
            <StyledLabel>Nyitás</StyledLabel>
            <div className='flex flex-col sm:flex-row sm:flex-wrap gap-4 sm:gap-6 items-start sm:items-center w-full'>
              <div className='flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3.5 w-full sm:w-auto'>
                <StyledLabel>Napja:</StyledLabel>
                <StyledInput type='date' size='large' />
              </div>

              <div className='flex flex-col sm:flex-row sm:items-center gap-2 w-full sm:w-auto'>
                <StyledLabel>Ideje:</StyledLabel>
                <div className='flex items-center gap-2 w-full sm:w-auto text-black'>
                  <input type='time' className='rounded-2xl p-2 text-xl bg-white w-full sm:w-auto' step={900} />
                  <span className='mx-1 text-[#ff9860] font-semibold mt-2'>–</span>
                  <input type='time' className='rounded-2xl p-2 text-xl bg-white w-full sm:w-auto' step={900} />
                </div>
              </div>

              <div className='flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-3.5 w-full sm:w-auto'>
                <StyledLabel>Helye:</StyledLabel>
                <StyledInput type='text' placeholder='pl. 13. konyha' size='medium' />
              </div>
            </div>
          </div>
        </div>

        {/* Megjegyzés */}
        <div className='bg-[#2f2173] text-white p-4 rounded-2xl border-2 border-[#ff9860] mb-5 w-full'>
          <StyledLabel>Megjegyzés</StyledLabel>
          <textarea
            placeholder='pl. különleges nyitás, szokásosnál több foodexes kell, stb... (max 200 karakter lehet)'
            maxLength={200}
            className='bg-white w-full p-3 rounded-2xl text-black text-xl h-32 mt-4'
          />
        </div>

        {/* Gombok */}
        <div className='flex flex-col sm:flex-row justify-between items-center gap-4 sm:gap-0 w-full'>
          <div className='flex flex-col sm:flex-row gap-4 sm:gap-4 w-full sm:w-auto'>
            <Button label='Adatok betöltése' variant='secondary' />
            <Button label='Adatok mentése' variant='secondary' />
          </div>
          <Button label='Kérés leadása' variant='primary' />
        </div>
      </div>
    </div>
  );
}
