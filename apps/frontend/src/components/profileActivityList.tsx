export type ProfileActivityItem = {
  id: number;
  groupName: string;
  day: string;
  time: string;
  location: string;
  date: string;
  status?: 'accepted' | 'pending';
};

type ProfileActivityListProps = {
  items: ProfileActivityItem[];
  emptyLabel: string;
};

const STATUS_LABEL: Record<NonNullable<ProfileActivityItem['status']>, string> = {
  accepted: 'Elfogadva',
  pending: 'Folyamatban',
};

export function ProfileActivityList({ items, emptyLabel }: ProfileActivityListProps) {
  if (items.length === 0) {
    return <p className='text-gray-300 italic'>{emptyLabel}</p>;
  }

  return (
    <div className='flex flex-col gap-2 max-h-60 overflow-y-auto pr-1'>
      {items.map((item) => (
        <div
          key={item.id}
          className='grid grid-cols-1 sm:grid-cols-[minmax(7rem,1.2fr)_minmax(6rem,auto)_minmax(8rem,auto)_1fr_auto_auto] gap-x-3 gap-y-1 items-center bg-white/10 text-white rounded-xl px-3 py-2'
        >
          <span className='font-semibold text-[#FF9860]'>{item.groupName}</span>
          <span>{item.day}</span>
          <span>{item.time}</span>
          <span className='italic'>{item.location}</span>
          <span>{item.date}</span>
          {item.status && (
            <span
              className={`justify-self-start sm:justify-self-end text-xs font-bold px-2 py-0.5 rounded-full ${
                item.status === 'accepted' ? 'bg-green-200 text-green-900' : 'bg-white text-[#332C81]'
              }`}
            >
              {STATUS_LABEL[item.status]}
            </span>
          )}
        </div>
      ))}
    </div>
  );
}
