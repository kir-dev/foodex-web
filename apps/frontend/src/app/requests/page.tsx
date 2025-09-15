'use client';
import { ApprovedShiftsContainer } from '@/components/approvedShiftsContainer';
import { IncomingRequestsContainer } from '@/components/incomingRequestsContainer';

export default function RequestsPage() {
  return (
    <main className='p-6 flex flex-col items-center gap-6'>
      {/* Bejövő kérések */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Bejövő kérések</h3>
        <IncomingRequestsContainer
          requests={[
            {
              groupName: 'Vödör',
              day: 'Hétfő',
              time: '18:00',
              location: 'Nagykonyha',
              date: '09.15',
            },
            {
              groupName: 'Magyarosch',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
            {
              groupName: 'Akármi',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
            {
              groupName: 'Valami',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
            {
              groupName: 'Bármi',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
          ]}
        />
      </div>

      {/* Elfogadott műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Elfogadott műszakok</h3>
        <ApprovedShiftsContainer
          shifts={[
            {
              groupName: 'Vödör',
              day: 'Hétfő',
              time: '18:00',
              location: 'Nagykonyha',
              date: '09.15',
            },
            {
              groupName: 'Magyarosch',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
            {
              groupName: 'Valami',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
            {
              groupName: 'Akármi',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
            {
              groupName: 'jjvjsbvh',
              day: 'Csütürtök',
              time: '20:00',
              location: '15. konyha',
              date: '09-17',
            },
          ]}
        />
      </div>
    </main>
  );
}
