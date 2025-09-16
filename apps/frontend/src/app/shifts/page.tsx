'use client';
import { ActiveShiftsContainer } from '@/components/activeShiftsContainer';
import { SubmitShiftsContainer } from '@/components/submitShiftsContainer';

export default function ShiftsPage() {
  return (
    <main className='p-6 flex flex-col items-center gap-6'>
      {/* Aktív műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Aktív műszakok</h3>
        <ActiveShiftsContainer
          shifts={[
            { groupName: 'Vödör', day: 'Hétfő', time: '18:00', location: 'Nagykonyha', date: '09.15' },
            { groupName: 'Magyarosch', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
            { groupName: 'Akármi', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
            { groupName: 'Valami', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
            { groupName: 'Bármi', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
          ]}
        />
      </div>

      {/* Betelt műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Betelt műszakok</h3>
        <SubmitShiftsContainer
          shifts={[
            { groupName: 'Vödör', day: 'Hétfő', time: '18:00', location: 'Nagykonyha', date: '09.15' },
            { groupName: 'Magyarosch', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
            { groupName: 'Valami', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
            { groupName: 'Akármi', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
            { groupName: 'jjvjsbvh', day: 'Csütürtök', time: '20:00', location: '15. konyha', date: '09-17' },
          ]}
        />
      </div>
    </main>
  );
}
