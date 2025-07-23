'use client';

import Button from '@/components/button';
import { StyledInput } from '@/components/styledInput';
import { StyledLabel } from '@/components/styledLabel';

export default function RequestingPage() {
  return (
    <div className='px-[80px] py-8 flex flex-col items-center'>
      <div className='w-full max-w-[1280px] border-2 border-[#2f2173] rounded-2xl p-8'>
        {/* Kör neve + Nyitás */}
        <div className='flex flex-col md:flex-row gap-6 pb-5'>
          {/* Kör neve */}
          <div className='bg-[#2f2173] text-white p-4 rounded-2xl border-2 border-[#ff9860] w-full md:w-1/3'>
            <StyledLabel>Kör neve</StyledLabel>
            <StyledInput type='text' placeholder='pl. Pizzásch, Vödör, Kakas...' size='full' />
          </div>

          {/* Nyitás */}
          <div className='bg-[#2f2173] text-white p-4 rounded-2xl border-2 border-[#ff9860] flex-1'>
            <StyledLabel>Nyitás</StyledLabel>
            <div className='flex flex-wrap gap-6 items-center'>
              <div className='flex items-center gap-3.5'>
                <StyledLabel>Napja:</StyledLabel>
                <StyledInput type='date' size='large' />
              </div>

              <div className='flex items-center gap-3.5'>
                <StyledLabel>Ideje:</StyledLabel>
                <StyledInput type='text' placeholder='pl. 7:30-10' size='small' />
              </div>

              <div className='flex items-center gap-3.5'>
                <StyledLabel>Helye:</StyledLabel>
                <StyledInput type='text' placeholder='pl. 13. konyha' size='medium' />
              </div>
            </div>
          </div>
        </div>

        {/* Megjegyzés */}
        <div className='bg-[#2f2173] text-white p-4 rounded-2xl border-2 border-[#ff9860] mb-5'>
          <StyledLabel>Megjegyzés</StyledLabel>
          <textarea
            placeholder='pl. különleges nyitás, szokásosnál több foodexes kell, stb... (max 200 karakter lehet)'
            maxLength={200}
            className='bg-white w-full p-3 rounded-2xl text-black text-xl h-32 mt-4'
          />
        </div>

        {/* Gombok */}
        <div className='flex justify-between items-center'>
          <div className='flex gap-4'>
            <Button label='Adatok betöltése' variant='secondary' />
            <Button label='Adatok mentése' variant='secondary' />
          </div>
          <Button label='Kérés leadása' variant='primary' />
        </div>
      </div>
    </div>
  );
}
