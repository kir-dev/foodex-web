type Request = {
  groupName: string;
  day: string;
  time: string;
  location: string;
  date: string;
};

type RequestsProps = {
  requests: Request[];
};

export function IncomingRequestsContainer({ requests }: RequestsProps) {
  return (
    <div className='w-full rounded-xl p-2 sm:p-3'>
      <div className='flex flex-col gap-2 max-h-60 overflow-y-auto w-full'>
        {requests.map((request) => (
          <div
            key={`${request.groupName}-${request.date}-${request.time}`}
            className='grid grid-cols-1 sm:grid-cols-[1fr_1fr_1fr_1fr_1fr_auto] items-center
                       bg-[#332C81] text-[#FF9860] font-semibold text-base sm:text-lg rounded-xl w-full'
          >
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>
              {request.groupName}
            </span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{request.day}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{request.time}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{request.location}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{request.date}</span>

            <div className='flex justify-end px-2 py-1 w-full'>
              <button className='bg-white text-[#332C81] font-bold px-3 py-1 rounded-xl w-fit'>Elfogadás</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
