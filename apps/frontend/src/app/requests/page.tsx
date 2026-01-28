import { ApprovedShiftsContainer } from '@/components/approvedShiftsContainer';
import { IncomingRequestsContainer } from '@/components/incomingRequestsContainer';
import { RequestPageData } from '@/types/requestPageData';

export default async function RequestsPage() {
  const response = await fetch('http://localhost:8080/api/incoming-requests');
  const data: RequestPageData = await response.json();
  return (
    <main className='p-6 flex flex-col items-center gap-6'>
      {/* Bejövő kérések */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Bejövő kérések</h3>
        <IncomingRequestsContainer
          requests={data.incomingFoodExRequests.map((req) => ({
            groupName: `Kör ID: ${req.cookingClubId}`,
            day: new Date(req.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
            time: new Date(req.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
            location: req.place,
            date: new Date(req.opening)
              .toLocaleDateString('hu-HU', { month: '2-digit', day: '2-digit' })
              .replace('.', '-'),
          }))}
        />
      </div>

      {/* Elfogadott műszakok */}
      <div className='w-full max-w-5xl border-2 border-[#332C81] rounded-xl p-2'>
        <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Elfogadott műszakok</h3>
        <ApprovedShiftsContainer
          shifts={data.acceptedShifts.map((shift) => ({
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
