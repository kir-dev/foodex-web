'use client';

import { Menu, X } from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';
import { useEffect, useState } from 'react';

interface NavItem {
  href: string;
  title: string;
  label?: string;
  isProfile?: boolean;
}

function Navbar() {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState<boolean | null>(null); // null = még töltődik

  // Ellenőrizzük, be van-e jelentkezve a felhasználó
  useEffect(() => {
    // Egy olyan végpontot hívunk, ami csak bejelentkezve érhető el, vagy a profil adatokat adja vissza
    fetch('/backend-api/incoming-requests', { credentials: 'include' })
      .then((res) => {
        if (res.status === 401 || res.status === 403) {
          setIsLoggedIn(false);
        } else if (res.ok) {
          setIsLoggedIn(true);
        } else {
          setIsLoggedIn(false);
        }
      })
      .catch(() => {
        setIsLoggedIn(false);
      });
  }, []);

  const navItemsLeft: NavItem[] = [
    { href: '/home', title: 'Kezdőlap', label: 'Kezdőlap' },
    // Csak akkor mutatjuk a kérést, ha be van jelentkezve
    ...(isLoggedIn ? [{ href: '/requesting', title: 'FoodEx kérés', label: 'FoodEx kérés' }] : []),
  ];

  const navItemsRight: NavItem[] = isLoggedIn
    ? [
        { href: '/openings', title: 'Nyitások', label: 'Nyitások' },
        { href: '/requests', title: 'Kérések', label: 'Kérések' },
        { href: '/shifts', title: 'Műszakok', label: 'Műszakok' },
        { href: '/profile', title: 'Profil', isProfile: true },
      ]
    : [];

  // Az AuthSch bejelentkezési URL a te Spring Boot konfigurációd alapján
  const loginUrl = 'http://localhost:8080/oauth2/authorization/authsch';

  return (
    <nav className='w-full bg-white border-b-2 border-[#332C81] px-4 py-2'>
      <div className='flex justify-between items-center'>
        <div className='text-[#332C81] text-2xl font-bold block sm:hidden'>FoodEx</div>

        <button className='sm:hidden text-[#332C81]' onClick={() => setIsOpen(!isOpen)}>
          {isOpen ? <X size={32} /> : <Menu size={32} />}
        </button>

        <div className='hidden sm:flex justify-between w-full'>
          {/* Bal oldal */}
          <div className='flex space-x-4'>
            {navItemsLeft.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                title={item.title}
                className='px-3 py-1 border-2 border-[#332C81] rounded-md text-[#332C81] text-2xl font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
              >
                {item.label}
              </Link>
            ))}
          </div>

          {/* Jobb oldal */}
          <div className='flex space-x-4 items-center'>
            {isLoggedIn === false ? (
              // HA NINCS BEJELENTKEZVE: Bejelentkezés gomb az AuthSch-ra
              <a
                href={loginUrl}
                className='px-4 py-1 border-2 border-[#FF9860] bg-[#332C81] rounded-md text-white text-2xl font-semibold hover:bg-white hover:text-[#332C81] hover:border-[#332C81] transition-all'
              >
                Bejelentkezés
              </a>
            ) : isLoggedIn === true ? (
              // HA BE VAN JELENTKEZVE: Menüpontok + Profilkép
              navItemsRight.map((item) =>
                item.isProfile ? (
                  <Link key={item.href} href={item.href} title={item.title}>
                    <Image
                      src='/profile.png'
                      alt='Profil'
                      width={42}
                      height={42}
                      className='rounded-full border-2 border-[#332C81] hover:bg-[#332C81] transition-all p-1 hover:p-1.5'
                    />
                  </Link>
                ) : (
                  <Link
                    key={item.href}
                    href={item.href}
                    title={item.title}
                    className='px-3 py-1 border-2 border-[#332C81] rounded-md text-[#332C81] text-2xl font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
                  >
                    {item.label}
                  </Link>
                )
              )
            ) : (
              // Töltési állapot amíg az API válaszol
              <div className='text-gray-400 text-lg'>Ellenőrzés...</div>
            )}
          </div>
        </div>
      </div>

      {/* Mobil menü lenyíló */}
      {isOpen && (
        <div className='sm:hidden mt-2 flex flex-col space-y-2'>
          {navItemsLeft.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              title={item.title}
              onClick={() => setIsOpen(false)}
              className='px-3 py-2 border-2 border-[#332C81] rounded-md text-[#332C81] text-lg font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
            >
              {item.label}
            </Link>
          ))}

          {isLoggedIn === false ? (
            <a
              href={loginUrl}
              className='px-3 py-2 text-center border-2 border-[#FF9860] bg-[#332C81] rounded-md text-white text-lg font-semibold'
            >
              Bejelentkezés
            </a>
          ) : (
            navItemsRight.map((item) =>
              item.isProfile ? (
                <Link
                  key={item.href}
                  href={item.href}
                  title={item.title}
                  onClick={() => setIsOpen(false)}
                  className='flex items-center space-x-2 px-3 py-2 border-2 border-[#332C81] rounded-md text-[#332C81] text-lg font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
                >
                  <Image
                    src='/profile.png'
                    alt='Profil'
                    width={32}
                    height={32}
                    className='rounded-full border border-[#332C81] p-0.5'
                  />
                  <span>Profil</span>
                </Link>
              ) : (
                <Link
                  key={item.href}
                  href={item.href}
                  title={item.title}
                  onClick={() => setIsOpen(false)}
                  className='px-3 py-2 border-2 border-[#332C81] rounded-md text-[#332C81] text-lg font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
                >
                  {item.label}
                </Link>
              )
            )
          )}
        </div>
      )}
    </nav>
  );
}

export default Navbar;
