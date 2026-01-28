import { ActiveShiftsContainer } from '@/components/activeShiftsContainer';
import { SubmitShiftsContainer } from '@/components/submitShiftsContainer';
import { ShiftsPageData } from '@/types/shiftspagedata';

export default async function ShiftsPage() {
  const response = await fetch('http://localhost:8080/api/shifts');
  const data: ShiftsPageData = await response.json();
  return (
    <main className='p-6 flex flex-col items-center gap-6'>
      {/* Aktív műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Aktív műszakok</h3>
        <ActiveShiftsContainer
          shifts={data.activeShifts.map((shift) => ({
            groupName: `Kör #${shift.cookingClubId}`,
            day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
            location: shift.place,
            date: new Date(shift.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
          }))}
        />
      </div>

      {/* Betelt műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Betelt műszakok</h3>
        <SubmitShiftsContainer
          shifts={data.fullShifts.map((shift) => ({
            groupName: `Kör #${shift.cookingClubId}`,
            day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
            location: shift.place,
            date: new Date(shift.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
          }))}
        />
      </div>
    </main>
  );
}
