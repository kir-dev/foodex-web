'use client';
import {
  boxContainer,
  inputFullWidth,
  inputLarge,
  inputMedium,
  inputSmall,
  labelStyle,
  sectionLabel,
} from '@/app/requesting/requestingStyles';
import Button from '@/components/button';

export default function RequestingPage() {
  return (
    <div className='px-[80px] py-8 flex flex-col items-center'>
      <div className='w-full max-w-[1200px] space-y-5'>
        {/* Kör neve + Nyitás */}
        <div className='flex flex-col md:flex-row gap-6'>
          <div className={`${boxContainer} w-full md:w-1/3`}>
            <label className={sectionLabel}>Kör neve</label>
            <input type='text' placeholder='pl. Pizzásch, Vödör, Kakas...' className={inputFullWidth} />
          </div>

          <div className={`${boxContainer} flex-1`}>
            <label className={sectionLabel}>Nyitás</label>
            <div className='flex flex-wrap gap-6 items-center'>
              <div className='flex items-center gap-3.5'>
                <label className={labelStyle}>Napja:</label>
                <input type='date' className={inputLarge} />
              </div>
              <div className='flex items-center gap-3.5'>
                <label className={labelStyle}>Ideje:</label>
                <input type='text' placeholder='pl. 7:30-10' className={inputSmall} />
              </div>
              <div className='flex items-center gap-3.5'>
                <label className={labelStyle}>Helye:</label>
                <input type='text' placeholder='pl. 13. konyha' className={inputMedium} />
              </div>
            </div>
          </div>
        </div>

        {/* Megjegyzés */}
        <div className={boxContainer}>
          <label className={sectionLabel}>Megjegyzés</label>
          <textarea
            placeholder='pl. különleges nyitás, szokásosnál több foodexes kell, stb... (max 200 karakter lehet)'
            maxLength={200}
            className='bg-white w-full p-3 rounded-2xl text-black text-xl h-32'
          />
        </div>

        {/* Gombok */}
        <div className='flex justify-between items-center'>
          <div className='flex gap-4'>
            <Button label='Adatok betöltése' />
            <Button label='Adatok mentése' />
          </div>
          <Button label='Kérés leadása' />
        </div>
      </div>
    </div>
  );
}
