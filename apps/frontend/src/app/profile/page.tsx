'use client';

export default function ProfilePage() {
  return (
    <div className='w-full flex justify-center p-4 sm:p-6'>
      {/* Külső wrapper, ami összefogja az egészet */}
      <div className=' rounded-xl border-2 border-[#332C81] p-4 sm:p-8 w-full max-w-6xl space-y-6'>
        {/* Felső rész */}
        <div className='flex flex-col md:flex-row gap-6'>
          {/* Bal oldali kép placeholder */}
          <div className='w-48 h-48 sm:w-60 sm:h-60 md:w-72 md:h-72 bg-gray-300 rounded-xl mx-auto md:mx-0 border-2 border-[#FF9860]' />

          {/* Jobb oldali infó */}
          <div className='flex-1 bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4'>
            {/* Név + Becenév */}
            <div className='mb-4 flex flex-col gap-2 md:flex-row md:items-center md:gap-4'>
              <div className='flex items-center gap-2 text-xl font-semibold'>
                <span className='text-[#FF9860]'>Név:</span>
                <span className='text-white'>Sali Nóra</span>
              </div>

              <div className='flex flex-col md:flex-row md:items-center gap-2 w-full md:w-auto'>
                <span className='text-[#FF9860] font-semibold text-xl'>Becenév:</span>
                <input
                  type='text'
                  className='rounded-xl px-2 py-1 w-full md:w-48 bg-white text-black placeholder-gray-300'
                  placeholder='Add meg a beceneved'
                />
              </div>
            </div>

            {/* Email */}
            <div className='mb-2 text-xl font-semibold'>
              <span className='text-[#FF9860]'>E-mail:</span> <span className='text-white'>salinora2005@gmail.com</span>
            </div>

            {/* Jogosultság */}
            <div className='mb-2 text-xl font-semibold'>
              <span className='text-[#FF9860]'>Jogosultság:</span> <span className='text-white'>admin</span>
            </div>

            {/* Idézet */}
            <div>
              <span className='text-[#FF9860] font-semibold text-xl'>Kedvenc idézet</span>
              <textarea
                className='w-full h-20 rounded-xl px-2 py-1 mt-3 bg-white text-black placeholder-gray-300'
                placeholder='Írd ide az idézeted'
              />
            </div>
          </div>
        </div>

        {/* Alsó rész */}
        <div className='bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4'>
          <h2 className='text-[#FF9860] font-semibold mb-2 text-2xl tracking-wide'>Féléves tevékenységek</h2>
          <div className='h-40 bg-[#332C81]' />
        </div>
      </div>
    </div>
  );
}
