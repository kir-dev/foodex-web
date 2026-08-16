'use client';

import { UserNameLink } from '@/components/userNameLink';

export type ShiftWorker = {
  id: number;
  nickname: string;
};

export type Shift = {
  id: number;
  groupName: string;
  day: string;
  time: string;
  location: string;
  date: string;
  occupancy?: string;
  workers?: ShiftWorker[];
  joined?: boolean;
  canJoin?: boolean;
  canLeave?: boolean;
};

type ShiftTableProps = {
  shifts: Shift[];
  buttons?: { label: string; onClick: (shift: Shift) => void; hidden?: (shift: Shift) => boolean }[];
  maxHeight?: string;
  showNamesColumn?: boolean;
  emptyLabel?: string;
};

const DEFAULT_BUTTONS: { label: string; onClick: (shift: Shift) => void }[] = [];

export function ShiftTable({
  shifts,
  buttons = DEFAULT_BUTTONS,
  maxHeight = 'max-h-[70vh]',
  showNamesColumn = false,
  emptyLabel = 'Nincs megjeleníthető műszak.',
}: ShiftTableProps) {
  if (shifts.length === 0) {
    return <p className='px-3 py-4 text-[#332C81]'>{emptyLabel}</p>;
  }

  return (
    <div className='w-full rounded-xl p-2 sm:p-3'>
      <div className={`flex flex-col gap-2 overflow-y-auto w-full ${maxHeight}`}>
        {shifts.map((shift) => (
          <div
            key={shift.id}
            className={`grid grid-cols-1 ${
              showNamesColumn
                ? 'sm:grid-cols-[1fr_1fr_1fr_1fr_1.6fr_auto]'
                : 'sm:grid-cols-[1fr_1fr_1fr_1fr_auto]'
            } items-center bg-[#332C81] text-[#FF9860] font-semibold text-base sm:text-lg rounded-xl w-full ${
              shift.joined ? 'ring-2 ring-[#FF9860]' : ''
            }`}
          >
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>
              {shift.groupName}
              {shift.joined && (
                <span className='ml-2 text-xs font-bold bg-[#FF9860] text-[#332C81] px-2 py-0.5 rounded-full align-middle'>
                  Jelentkeztél
                </span>
              )}
            </span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>
              <span className='block'>{shift.day}</span>
              <span className='block text-sm font-medium text-white'>{shift.date}</span>
            </span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{shift.time}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{shift.location}</span>

            {showNamesColumn && (
              <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full text-sm sm:text-base font-medium text-white'>
                {shift.occupancy && <span className='block text-[#FF9860]'>{shift.occupancy}</span>}
                <span className='italic font-normal'>
                  {shift.workers && shift.workers.length > 0
                    ? shift.workers.map((worker, index) => (
                        <span key={worker.id}>
                          {index > 0 ? ', ' : ''}
                          <UserNameLink userId={worker.id}>{worker.nickname}</UserNameLink>
                        </span>
                      ))
                    : 'Még senki'}
                </span>
              </span>
            )}

            <div className='flex justify-end gap-2 px-2 py-1 w-full'>
              {buttons
                .filter((btn) => !btn.hidden || !btn.hidden(shift))
                .map((btn) => (
                  <button
                    key={btn.label}
                    type='button'
                    className='bg-white text-[#332C81] font-bold px-3 py-1 rounded-xl w-fit'
                    onClick={() => btn.onClick(shift)}
                  >
                    {btn.label}
                  </button>
                ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
