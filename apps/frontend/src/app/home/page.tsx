'use client';

import { ImageContainer } from '@/components/imageContainer';
import { MembersContainer } from '@/components/membersContainer';
import { OpeningsContainer } from '@/components/openingsContainer';
import { HomePageData } from '@/types/homePageData';
import Image from 'next/image';

export default async function HomePage() {
  const response = await fetch('https://api.foodex.kir-dev.hu/home');
  const data: HomePageData = await response.json();

  return (
    <main className='flex flex-col items-center justify-start min-h-screen p-6 bg-white'>
      {/* Felső konténer: Kép + szöveg */}
      <div className='flex flex-col md:flex-row gap-4 w-full max-w-7xl lg:max-w-[90%] mb-4'>
        <ImageContainer>
          <Image src='/kep1.jpg' alt='Kép' width={500} height={500} className='w-full h-full object-cover' />
        </ImageContainer>

        <div className='flex flex-col justify-start w-full bg-[#332C81] text-[#FF9860] rounded-xl px-6 py-6'>
          <h2 className='text-3xl sm:text-4xl md:text-5xl mb-4 font-semibold'>FoodEx</h2>
          <p className='text-base sm:text-lg'>
            A FoodEx kör 2003-ban alakult meg, azóta aktívan tevékenykedik a Schönherz koliban. Biztosan hallottatok már
            a kajás körökről is a söciben, akik nyitásokat szerveznek és kaját csinálnak megrendelésre a hét legtöbb
            napján. Velünk ezen nyitások idejében találkozhattok, hiszen mi vagyunk a kajaszállítók/futárok, akik ezen
            körök, vagyis: az Americano, Vödör, Reggelisch, Paschta, Lángosch, Pizzásch, Kakas és Magyarosch rendeléseit
            a kolin belül kiszállítjuk a szobákba. ...
          </p>
        </div>
      </div>

      {/* A hét feelingje */}
      <div className='w-full sm:w-2/3 md:w-1/3 border-2 border-[#332C81] rounded-xl p-2 mb-4 text-center'>
        <p className='text-xl font-semibold text-[#332C81]'>A hét feelingje: --</p>
      </div>

      <div className='w-full max-w-7xl grid grid-cols-1 lg:grid-cols-[2fr_1fr] gap-6'>
        {/* Aktív tagok */}
        <div className='w-full border-2 border-[#332C81] rounded-xl p-2'>
          <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Aktív tagok</h3>
          {/*Példa adatok kinézet ellenőrzéshez*/}
          <MembersContainer
            members={data.activeMembers.map((member) => ({
              name: member.name,
              quote: member.favoriteQuote,
            }))}
          />
        </div>

        {/* Heti nyitások */}
        <div className='w-full border-2 border-[#332C81] rounded-xl p-2'>
          <h3 className='text-2xl font-bold text-[#332C81] pl-3'>Heti nyitások</h3>
          {/*Példa adatok kinézet ellenőrzéshez*/}
          <OpeningsContainer
            openings={data.upcomgingShifts.map((shift) => ({
              groupName: shift.cookingClubId.toString(),
              day: new Date(shift.opening).toLocaleDateString('hu-HU', { weekday: 'long' }),
              time:
                new Date(shift.opening).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }) +
                '-' +
                new Date(shift.closing).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' }),
              location: shift.place,
            }))}
          />
        </div>
      </div>
    </main>
  );
}
