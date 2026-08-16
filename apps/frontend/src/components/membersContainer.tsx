import { UserNameLink } from '@/components/userNameLink';

type Member = {
  id: number;
  name: string;
  title: string;
};

type Props = {
  members: Member[];
};

export function MembersContainer({ members }: Props) {
  return (
    <div className='w-full rounded-xl p-3'>
      <div className='flex flex-col gap-2 min-h-72 max-h-[32rem] overflow-y-auto pr-1'>
        {members.map((member) => (
          <div
            key={member.id}
            className='border-2 border-[#332C81] bg-[#332C81] rounded-xl p-2 flex items-center text-white text-lg'
          >
            <span className='font-semibold text-[#FF9860]'>
              <UserNameLink userId={member.id}>{member.name}</UserNameLink>{' '}
              <span className='font-normal italic text-white'>({member.title})</span>
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
