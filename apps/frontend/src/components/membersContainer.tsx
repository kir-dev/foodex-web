type Member = {
  name: string;
  quote: string;
};

type Props = {
  members: Member[];
};

export function MembersContainer({ members }: Props) {
  return (
    <div className='w-full max-w-6xl rounded-xl p-2 mb-4'>
      <div className='flex flex-col gap-2 max-h-32 overflow-y-auto pr-1'>
        {members.map((member, index) => (
          <div
            key={index}
            className='border-2 border-[#332C81] bg-[#332C81] rounded-xl p-2 flex items-center gap-2 text-white text-lg'
          >
            <span className='font-semibold text-[#FF9860]'>{member.name}</span>
            <span className='italic'>– {member.quote}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
