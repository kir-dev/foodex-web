'use client';

import { UserNameLink } from '@/components/userNameLink';
import { formatShortDate, formatTimeRange, formatWeekday } from '@/lib/dates';
import { DetailedOpeningRequestDto } from '@/types/api';

export type OpeningRequestSubmitter = {
  id: number;
  nickname: string;
};

export type OpeningRequestRow = {
  id: number;
  groupName: string;
  day: string;
  time: string;
  location: string;
  date: string;
  submittedBy: OpeningRequestSubmitter;
};

type OpeningRequestButton = {
  label: string;
  onClick: (request: OpeningRequestRow) => void;
  hidden?: (request: OpeningRequestRow) => boolean;
};

type OpeningRequestTableProps = {
  requests: OpeningRequestRow[];
  buttons?: OpeningRequestButton[];
  maxHeight?: string;
  emptyLabel?: string;
};

const DEFAULT_BUTTONS: { label: string; onClick: (request: OpeningRequestRow) => void }[] = [];

export function requestToRow(request: DetailedOpeningRequestDto): OpeningRequestRow {
  return {
    id: request.id,
    groupName: request.cookingClub?.name || `Kör ID: ${request.cookingClub?.id ?? request.id}`,
    day: formatWeekday(request.opening),
    time: formatTimeRange(request.opening, request.closing),
    location: request.place,
    date: formatShortDate(request.opening),
    submittedBy: {
      id: request.user.id,
      nickname: request.user.nickname,
    },
  };
}

export function OpeningRequestTable({
  requests,
  buttons = DEFAULT_BUTTONS,
  maxHeight = 'max-h-[70vh]',
  emptyLabel = 'Nincs megjeleníthető kérés.',
}: OpeningRequestTableProps) {
  if (requests.length === 0) {
    return <p className='px-3 py-4 text-[#332C81]'>{emptyLabel}</p>;
  }

  return (
    <div className='w-full rounded-xl p-2 sm:p-3'>
      <div className={`flex flex-col gap-2 overflow-y-auto w-full ${maxHeight}`}>
        {requests.map((request) => (
          <div
            key={request.id}
            className='grid grid-cols-1 sm:grid-cols-[1fr_1fr_1fr_1fr_auto] items-center bg-[#332C81] text-[#FF9860] font-semibold text-base sm:text-lg rounded-xl w-full'
          >
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>
              {request.groupName}
              <span className='block text-sm font-medium text-white'>
                Kérte:{' '}
                <UserNameLink userId={request.submittedBy.id}>{request.submittedBy.nickname}</UserNameLink>
              </span>
            </span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>
              <span className='block'>{request.day}</span>
              <span className='block text-sm font-medium text-white'>{request.date}</span>
            </span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{request.time}</span>
            <span className='px-2 py-2 border-b sm:border-b-0 sm:border-r border-white w-full'>{request.location}</span>

            <div className='flex justify-end gap-2 px-2 py-1 w-full'>
              {buttons
                .filter((btn) => !btn.hidden || !btn.hidden(request))
                .map((btn) => (
                  <button
                    key={btn.label}
                    type='button'
                    className='bg-white text-[#332C81] font-bold px-3 py-1 rounded-xl w-fit'
                    onClick={() => btn.onClick(request)}
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
