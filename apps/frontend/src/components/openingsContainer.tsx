type Opening = {
  id: number;
  groupName: string;
  day: string;
  time: string;
  location: string;
};

type OpeningsProps = {
  openings: Opening[];
};

export function OpeningsContainer({ openings }: OpeningsProps) {
  return (
    <div className='w-full rounded-xl p-3'>
      <div className='flex flex-col gap-1 pr-1 min-h-72 max-h-[32rem] overflow-y-auto'>
        {openings.map((opening) => (
          <div
            key={opening.id}
            className='border-2 border-[#332C81] bg-[#332C81] rounded-xl px-3 py-2 grid
    sm:grid-cols-[minmax(6rem,auto)_minmax(6rem,auto)_minmax(6rem,auto)_1fr]
    gap-x-3 items-center text-white text-lg'
          >
            <span className='font-semibold'>{opening.groupName}</span>
            <span>{opening.day}</span>
            <span>{opening.time}</span>
            <span className='italic'>{opening.location}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
