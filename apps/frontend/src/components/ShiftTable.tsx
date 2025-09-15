'use client';

export type Shift = {
  groupName: string;
  day: string;
  time: string;
  location: string;
  date: string;
};

type ShiftTableProps = {
  shifts: Shift[];
  buttons?: { label: string; onClick: (shift: Shift) => void }[];
  maxHeight?: string;
  showNamesColumn?: boolean;
};

const DEFAULT_BUTTONS: { label: string; onClick: (shift: Shift) => void }[] = [];

export function ShiftTable({
  shifts,
  buttons = DEFAULT_BUTTONS,
  maxHeight = 'max-h-60',
  showNamesColumn = false,
}: ShiftTableProps) {
  return (
    <div className='w-full rounded-xl p-2 sm:p-3'>
      <div className={`flex flex-col gap-2 overflow-y-auto w-full ${maxHeight}`}>
        {shifts.map((shift) => (
          <div
            key={`${shift.groupName}-${shift.date}-${shift.time}`}
            className={`grid grid-cols-1 ${
              showNamesColumn
                ? 'sm:grid-cols-[1fr_1fr_1fr_1fr_1fr_1fr_1fr_auto]'
                : 'sm:grid-cols-[1fr_1fr_1fr_1fr_1fr_1fr_auto]'
            } items-center
                       bg-[#332C81] text-[#FF9860] font-semibold text-base sm:text-lg rounded-xl w-full`}
          >
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{shift.groupName}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{shift.day}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{shift.time}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{shift.location}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{shift.date}</span>

            {showNamesColumn && (
              <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>
                {/* ide jönnek majd a nevek */}
              </span>
            )}

            <div className='flex justify-end gap-2 px-2 py-1 w-full'>
              {buttons.map((btn) => (
                <button
                  key={btn.label}
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
