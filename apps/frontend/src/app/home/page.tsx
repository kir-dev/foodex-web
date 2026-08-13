'use client';

import { ImageContainer } from '@/components/imageContainer';
import { MembersContainer } from '@/components/membersContainer';
import { OpeningsContainer } from '@/components/openingsContainer';
import { PageState } from '@/components/page-state';
import { apiFetch, isApiError } from '@/lib/api';
import { formatTimeRange, formatWeekday } from '@/lib/dates';
import { HomepageDto } from '@/types/api';
import Image from 'next/image';
import { useEffect, useState } from 'react';

export default function HomePage() {
  const [data, setData] = useState<HomepageDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async (): Promise<void> => {
      try {
        const homePageData = await apiFetch<HomepageDto>('/api/homepage');
        setData(homePageData);
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült lekérni a főoldal adatait.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, []);

  if (loading) {
    return <PageState>Adatok betöltése...</PageState>;
  }

  if (error) {
    return <PageState variant='error'>Hiba történt: {error}</PageState>;
  }

  if (!data) {
    return null;
  }

  return (
    <main className='flex flex-col items-center justify-start min-h-screen p-6 bg-white'>
      <div className='flex flex-col md:flex-row gap-4 w-full max-w-7xl lg:max-w-[90%] mb-4'>
        <ImageContainer>
          <Image
            src={data.foodExLogo || '/kep1.jpg'}
            alt='FoodEx Logo'
            width={500}
            height={500}
            className='w-full h-full object-cover rounded-xl'
            unoptimized
          />
        </ImageContainer>

        <div className='flex flex-col justify-start w-full bg-[#332C81] text-[#FF9860] rounded-xl px-6 py-6'>
          <h2 className='text-3xl sm:text-4xl md:text-5xl mb-4 font-semibold'>FoodEx</h2>
          <p className='text-base sm:text-lg'>
            A FoodEx kör 2003-ban alakult meg, azóta aktívan tevékenykedik a Schönherz koliban...
          </p>
        </div>
      </div>

      <div className='w-full sm:w-2/3 md:w-1/3 border-2 border-[#332C81] rounded-xl p-2 mb-4 text-center'>
        <p className='text-xl font-semibold text-[#332C81]'>
          A hét feelingje: <span className='font-normal text-black'>{data.feelingOfTheWeek}</span>
        </p>
      </div>

      <div className='w-full max-w-7xl grid grid-cols-1 lg:grid-cols-[2fr_1fr] gap-6'>
        <div className='w-full border-2 border-[#332C81] rounded-xl p-2'>
          <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Aktív tagok</h3>
          <MembersContainer
            members={(data.activeMembers || []).map((member) => ({
              id: member.id,
              name: member.nickname || 'Névtelen tag',
              quote: `Rang: ${member.role}`,
            }))}
          />
        </div>

        <div className='w-full border-2 border-[#332C81] rounded-xl p-2'>
          <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Heti nyitások</h3>
          <OpeningsContainer
            openings={(data.upcomingOpenings || []).map((openingReq) => ({
              id: openingReq.id,
              groupName: openingReq.cookingClub?.name || 'Ismeretlen kör',
              day: formatWeekday(openingReq.opening),
              time: formatTimeRange(openingReq.opening, openingReq.closing),
              location: openingReq.place || 'Nincs megadva',
            }))}
          />
        </div>
      </div>
    </main>
  );
}
