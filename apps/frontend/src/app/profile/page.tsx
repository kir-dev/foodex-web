'use client';

import { useAuth } from '@/components/auth-provider';
import Button from '@/components/button';
import { PageState } from '@/components/page-state';
import { ProfileActivityItem, ProfileActivityList } from '@/components/profileActivityList';
import { RequireAuth } from '@/components/require-auth';
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
import { usePathname } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';

export default function ProfilePage() {
  return (
    <RequireAuth loadingLabel='Profil betöltése...'>
      <ProfileContent />
    </RequireAuth>
  );
}

function clubLabel(clubId: number, clubNames: Record<number, string>): string {
  return clubNames[clubId] || `Kör #${clubId}`;
}

function isRequestAccepted(request: OpeningRequestDto): boolean {
  return request.accepted ?? request.isAccepted ?? false;
}

function toActivityItem(
  item: ShiftDto | OpeningRequestDto,
  clubNames: Record<number, string>,
  status?: ProfileActivityItem['status']
): ProfileActivityItem {
  return {
    id: item.id,
    groupName: clubLabel(item.cookingClubId, clubNames),
    day: formatWeekday(item.opening),
    time: formatTimeRange(item.opening, item.closing),
    location: item.place,
    date: formatShortDate(item.opening),
    status,
  };
}

function ProfileContent() {
  const { user, refresh } = useAuth();
  const pathname = usePathname();
  const [nickname, setNickname] = useState(user?.nickname || '');
  const [favouriteQuote, setFavouriteQuote] = useState(user?.favouriteQuote || '');
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<{ text: string; isError: boolean } | null>(null);
  const [semester, setSemester] = useState<{ start: string; end: string } | null>(null);
  const [clubNames, setClubNames] = useState<Record<number, string>>({});
  const [activitiesError, setActivitiesError] = useState<string | null>(null);
  const [activitiesLoading, setActivitiesLoading] = useState(true);

  useEffect(() => {
    setNickname(user?.nickname || '');
    setFavouriteQuote(user?.favouriteQuote || '');
  }, [user]);

  useEffect(() => {
    const loadActivities = async (): Promise<void> => {
      setActivitiesLoading(true);

      const [configResult, clubsResult] = await Promise.allSettled([
        apiFetch<ConfigurationDto>('/api/config'),
        apiFetch<DetailedCookingClubDto[]>('/api/cooking-clubs'),
        refresh(),
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
  }, [pathname, refresh]);

  const semesterShifts = useMemo(() => {
    if (!user || !semester) {
      return [];
    }
    return [...(user.shifts ?? [])]
      .filter((shift) => isWithinSemester(shift.opening, shift.closing, semester.start, semester.end))
      .sort(compareByOpeningDesc)
      .map((shift) => toActivityItem(shift, clubNames));
  }, [user, semester, clubNames]);

  const semesterRequests = useMemo(() => {
    if (!user || !semester) {
      return [];
    }
    return [...(user.requests ?? [])]
      .filter((request) => isWithinSemester(request.opening, request.closing, semester.start, semester.end))
      .sort(compareByOpeningDesc)
      .map((request) => toActivityItem(request, clubNames, isRequestAccepted(request) ? 'accepted' : 'pending'));
  }, [user, semester, clubNames]);

  if (!user) {
    return <PageState>Profil betöltése...</PageState>;
  }

  const handleSave = async (): Promise<void> => {
    setIsSaving(true);
    setSaveMessage(null);

    try {
      const payload: UpdateUserDto = {
        nickname: nickname || null,
        favouriteQuote: favouriteQuote || null,
      };
      await apiFetch<DetailedUserDto>(`/api/users/${user.id}`, {
        method: 'PATCH',
        body: payload,
      });
      await refresh();
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
                  <input
                    type='text'
                    className='rounded-xl px-2 py-1 w-full md:w-48 bg-white text-black placeholder-gray-300'
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    placeholder='Add meg a beceneved'
                  />
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
                <textarea
                  className='w-full h-20 rounded-xl px-2 py-1 mt-3 bg-white text-black placeholder-gray-300'
                  value={favouriteQuote}
                  onChange={(e) => setFavouriteQuote(e.target.value)}
                  placeholder='Írd ide az idézeted'
                />
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
                <ProfileActivityList
                  items={semesterShifts}
                  emptyLabel='Nincsenek még műszakjaid ebben a félévben.'
                />
              </div>

              <div className='space-y-2'>
                <h3 className='text-white font-semibold text-xl'>Nyitási kérések ({semesterRequests.length})</h3>
                <ProfileActivityList
                  items={semesterRequests}
                  emptyLabel='Nincsenek még nyitási kéréseid ebben a félévben.'
                />
              </div>
            </>
          )}
        </div>

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
      </div>
    </div>
  );
}
