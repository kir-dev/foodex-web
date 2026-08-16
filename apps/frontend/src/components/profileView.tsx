'use client';

import Button from '@/components/button';
import { ProfileActivityItem, ProfileActivityList } from '@/components/profileActivityList';
import { RequestShiftsModal } from '@/components/requestShiftsModal';
import { apiFetch, isApiError } from '@/lib/api';
import { compareByOpeningDesc, formatShortDate, formatTimeRange, formatWeekday, isWithinSemester } from '@/lib/dates';
import {
  ConfigurationDto,
  DetailedCookingClubDto,
  DetailedUserDto,
  OpeningRequestDto,
  ShiftDto,
  UpdateUserDto,
} from '@/types/api';
import { useEffect, useMemo, useState } from 'react';

type ProfileViewProps = {
  user: DetailedUserDto;
  editable: boolean;
  onSaved?: () => Promise<void>;
};

function clubLabel(clubId: number, clubNames: Record<number, string>): string {
  return clubNames[clubId] || `Kör #${clubId}`;
}

function isRequestAccepted(request: OpeningRequestDto): boolean {
  return request.accepted ?? request.isAccepted ?? false;
}

function toActivityItem(
  item: ShiftDto | OpeningRequestDto,
  clubNames: Record<number, string>,
  status?: ProfileActivityItem['status'],
  onShowShifts?: () => void
): ProfileActivityItem {
  return {
    id: item.id,
    groupName: clubLabel(item.cookingClubId, clubNames),
    day: formatWeekday(item.opening),
    time: formatTimeRange(item.opening, item.closing),
    location: item.place,
    date: formatShortDate(item.opening),
    status,
    onShowShifts,
  };
}

export function ProfileView({ user, editable, onSaved }: ProfileViewProps) {
  const [nickname, setNickname] = useState(user.nickname || '');
  const [favouriteQuote, setFavouriteQuote] = useState(user.favouriteQuote || '');
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<{ text: string; isError: boolean } | null>(null);
  const [semester, setSemester] = useState<{ start: string; end: string } | null>(null);
  const [clubNames, setClubNames] = useState<Record<number, string>>({});
  const [activitiesError, setActivitiesError] = useState<string | null>(null);
  const [activitiesLoading, setActivitiesLoading] = useState(true);
  const [shiftsRequest, setShiftsRequest] = useState<{ id: number; clubName: string } | null>(null);

  useEffect(() => {
    setNickname(user.nickname || '');
    setFavouriteQuote(user.favouriteQuote || '');
  }, [user]);

  useEffect(() => {
    const loadActivities = async (): Promise<void> => {
      setActivitiesLoading(true);

      const [configResult, clubsResult] = await Promise.allSettled([
        apiFetch<ConfigurationDto>('/api/config'),
        apiFetch<DetailedCookingClubDto[]>('/api/cooking-clubs'),
      ]);

      if (configResult.status === 'fulfilled') {
        setSemester({
          start: configResult.value.startOfSemester,
          end: configResult.value.endOfSemester,
        });
        setActivitiesError(null);
      } else {
        setActivitiesError(
          isApiError(configResult.reason) ? configResult.reason.message : 'Nem sikerült betölteni a félév adatait.'
        );
      }

      if (clubsResult.status === 'fulfilled' && Array.isArray(clubsResult.value)) {
        setClubNames(
          clubsResult.value.reduce<Record<number, string>>((names, club) => {
            names[club.id] = club.name;
            return names;
          }, {})
        );
      }

      setActivitiesLoading(false);
    };

    void loadActivities();
  }, [user.id]);

  const semesterShifts = useMemo(() => {
    if (!semester) {
      return [];
    }
    return [...(user.shifts ?? [])]
      .filter((shift) => isWithinSemester(shift.opening, shift.closing, semester.start, semester.end))
      .sort(compareByOpeningDesc)
      .map((shift) => toActivityItem(shift, clubNames));
  }, [user, semester, clubNames]);

  const semesterRequests = useMemo(() => {
    if (!semester) {
      return [];
    }
    return [...(user.requests ?? [])]
      .filter((request) => isWithinSemester(request.opening, request.closing, semester.start, semester.end))
      .sort(compareByOpeningDesc)
      .map((request) => {
        const accepted = isRequestAccepted(request);
        return toActivityItem(
          request,
          clubNames,
          accepted ? 'accepted' : undefined,
          accepted
            ? () =>
                setShiftsRequest({
                  id: request.id,
                  clubName: clubLabel(request.cookingClubId, clubNames),
                })
            : undefined
        );
      });
  }, [user, semester, clubNames]);

  const handleSave = async (): Promise<void> => {
    setIsSaving(true);
    setSaveMessage(null);

    if (nickname.length > 10) {
      setSaveMessage({ text: 'A becenév legfeljebb 10 karakter lehet.', isError: true });
      setIsSaving(false);
      return;
    }

    try {
      const payload: UpdateUserDto = {
        nickname: nickname || null,
        favouriteQuote: favouriteQuote || null,
      };
      await apiFetch<DetailedUserDto>(`/api/users/${user.id}`, {
        method: 'PATCH',
        body: payload,
      });
      await onSaved?.();
      setSaveMessage({ text: 'Változtatások sikeresen mentve!', isError: false });
    } catch (err) {
      setSaveMessage({
        text: isApiError(err) ? err.message : 'Hiba történt a mentés során.',
        isError: true,
      });
    } finally {
      setIsSaving(false);
    }
  };

  const shiftsEmptyLabel = editable
    ? 'Nincsenek még műszakjaid ebben a félévben.'
    : 'Nincsenek műszakok ebben a félévben.';
  const requestsEmptyLabel = editable
    ? 'Nincsenek még nyitási kéréseid ebben a félévben.'
    : 'Nincsenek nyitási kérések ebben a félévben.';

  return (
    <div className='w-full flex justify-center p-4 sm:p-6'>
      <div className='rounded-xl border-2 border-[#332C81] p-4 sm:p-8 w-full max-w-6xl space-y-6'>
        <div className='flex flex-col md:flex-row gap-6'>
          {user.profilePicture ? (
            <img
              src={user.profilePicture}
              alt='Profilkép'
              className='w-48 h-48 sm:w-60 sm:h-60 md:w-72 md:h-72 rounded-xl mx-auto md:mx-0 border-2 border-[#FF9860] object-cover'
            />
          ) : (
            <div className='w-48 h-48 sm:w-60 sm:h-60 md:w-72 md:h-72 bg-gray-300 rounded-xl mx-auto md:mx-0 border-2 border-[#FF9860]' />
          )}

          <div className='flex-1 bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4 flex flex-col justify-between'>
            <div>
              <div className='mb-4 flex flex-col gap-2 md:flex-row md:items-center md:gap-4'>
                <div className='flex items-center gap-2 text-xl font-semibold'>
                  <span className='text-[#FF9860]'>Név:</span>
                  <span className='text-white'>{user.name}</span>
                </div>

                <div className='flex flex-col md:flex-row md:items-center gap-2 w-full md:w-auto'>
                  <span className='text-[#FF9860] font-semibold text-xl'>Becenév:</span>
                  {editable ? (
                    <input
                      type='text'
                      className='rounded-xl px-2 py-1 w-full md:w-48 bg-white text-black placeholder-gray-300'
                      value={nickname}
                      maxLength={10}
                      onChange={(e) => setNickname(e.target.value)}
                      placeholder='Add meg a beceneved'
                    />
                  ) : (
                    <span className='text-white text-xl font-semibold'>{user.nickname || user.name}</span>
                  )}
                </div>
              </div>

              <div className='mb-2 text-xl font-semibold'>
                <span className='text-[#FF9860]'>E-mail:</span> <span className='text-white'>{user.email}</span>
              </div>

              <div className='mb-2 text-xl font-semibold'>
                <span className='text-[#FF9860]'>Jogosultság:</span>{' '}
                <span className='text-white'>{user.role.toLowerCase()}</span>
              </div>

              <div className='mb-4'>
                <span className='text-[#FF9860] font-semibold text-xl'>Kedvenc idézet</span>
                {editable ? (
                  <textarea
                    className='w-full h-20 rounded-xl px-2 py-1 mt-3 bg-white text-black placeholder-gray-300'
                    value={favouriteQuote}
                    onChange={(e) => setFavouriteQuote(e.target.value)}
                    placeholder='Írd ide az idézeted'
                  />
                ) : (
                  <p className='text-white text-lg mt-3 whitespace-pre-wrap'>
                    {user.favouriteQuote || '—'}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className='bg-[#332C81] border-2 border-[#FF9860] rounded-xl p-4 space-y-5'>
          <h2 className='text-[#FF9860] font-semibold text-2xl tracking-wide'>Féléves tevékenységek</h2>

          {activitiesLoading ? (
            <p className='text-gray-300 italic'>Tevékenységek betöltése...</p>
          ) : activitiesError ? (
            <p className='text-red-300'>{activitiesError}</p>
          ) : (
            <>
              <div className='space-y-2'>
                <h3 className='text-white font-semibold text-xl'>Műszakok ({semesterShifts.length})</h3>
                <ProfileActivityList items={semesterShifts} emptyLabel={shiftsEmptyLabel} />
              </div>

              <div className='space-y-2'>
                <h3 className='text-white font-semibold text-xl'>Nyitási kérések ({semesterRequests.length})</h3>
                <ProfileActivityList items={semesterRequests} emptyLabel={requestsEmptyLabel} />
              </div>
            </>
          )}
        </div>

        {editable && (
          <div className='flex flex-col sm:flex-row sm:items-center gap-4 pt-2'>
            <Button
              label={isSaving ? 'Mentés...' : 'Profil mentése'}
              variant='primary'
              onClick={() => void handleSave()}
              disabled={isSaving}
            />

            {saveMessage && (
              <span className={`text-lg font-medium ${saveMessage.isError ? 'text-red-500' : 'text-green-600'}`}>
                {saveMessage.text}
              </span>
            )}
          </div>
        )}
      </div>

      {shiftsRequest && (
        <RequestShiftsModal
          requestId={shiftsRequest.id}
          clubName={shiftsRequest.clubName}
          onClose={() => setShiftsRequest(null)}
        />
      )}
    </div>
  );
}
