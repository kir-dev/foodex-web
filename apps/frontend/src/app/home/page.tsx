'use client';

import { ImageContainer } from '@/components/imageContainer';
import { MembersContainer } from '@/components/membersContainer';
import { OpeningsContainer } from '@/components/openingsContainer';
import { HomePageData } from '@/types/homePageData';
import Image from 'next/image';
import { useEffect, useState } from 'react';

export default function HomePage() {
  const [data, setData] = useState<HomePageData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // A WebSecurityConfig-od alapján a /api/homepage permitAll(),
    // így a Next.js proxy-don keresztül simán átmegy auth nélkül is.
    fetch('/backend-api/homepage', { credentials: 'include' })
      .then((res) => {
        if (!res.ok) {
          throw new Error('Nem sikerült lekérni a főoldal adatait.');
        }
        return res.json();
      })
      .then((homePageData: HomePageData | null) => {
        if (homePageData) {
          setData(homePageData);
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <div className='w-full min-h-screen flex items-center justify-center bg-white text-xl font-semibold text-[#332C81]'>
        Adatok betöltése...
      </div>
    );
  }

  if (error) {
    return (
      <div className='w-full min-h-screen flex items-center justify-center bg-white text-xl font-semibold text-red-500'>
        Hiba történt: {error}
      </div>
    );
  }

  if (!data) return null;

  return (
    <main className='flex flex-col items-center justify-start min-h-screen p-6 bg-white'>
      {/* Felső konténer: Kép + szöveg */}
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
            A FoodEx kör 2003-ban alakult meg, azóta aktívan tevékenykedik a Schönherz koliban. Biztosan hallottatok már
            a kajás körökről is a söciben, akik nyitásokat szerveznek és kaját csinálnak megrendelésre a hét legtöbb
            napján. Velünk ezen nyitások idejében találkozhattok, hiszen mi vagyunk a kajaszállítók/futárok, akik ezen
            körök, vagyis: az Americano, Vödör, Reggelisch, Paschta, Lángosch, Pizzásch, Kakas és Magyarosch rendeléseit
            a kolin binnen kiszállítjuk a szobákba. ...
          </p>
        </div>
      </div>

      {/* A hét feelingje - Most már dinamikusan a backendből! */}
      <div className='w-full sm:w-2/3 md:w-1/3 border-2 border-[#332C81] rounded-xl p-2 mb-4 text-center'>
        <p className='text-xl font-semibold text-[#332C81]'>
          A hét feelingje: <span className='font-normal text-black'>{data.feelingOfTheWeek}</span>
        </p>
      </div>

      <div className='w-full max-w-7xl grid grid-cols-1 lg:grid-cols-[2fr_1fr] gap-6'>
        {/* Aktív tagok (activeMembers -> UserDto) */}
        <div className='w-full border-2 border-[#332C81] rounded-xl p-2'>
          <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Aktív tagok</h3>
          <MembersContainer
            members={data.activeMembers.map((member) => ({
              name: member.nickname || 'Névtelen tag',
              quote: `Rang: ${member.role}`,
            }))}
          />
        </div>

        {/* Heti nyitások (A backendből érkező upcomingOpenings térképezése) */}
        <div className='w-full border-2 border-[#332C81] rounded-xl p-2'>
          <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Heti nyitások</h3>
          <OpeningsContainer
            openings={(data.upcomingOpenings || []).map((shift) => ({
              groupName: shift.cookingClub?.name || `Kör #${shift.cookingClubId}`,
              day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
              time:
                new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }) +
                ' - ' +
                new Date(shift.closing).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
              location: shift.place,
            }))}
          />
        </div>
      </div>
    </main>
  );
}
