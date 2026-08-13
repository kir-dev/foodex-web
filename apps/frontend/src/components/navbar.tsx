'use client';

import { loginUrl, useAuth } from '@/components/auth-provider';
import { Menu, X } from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';
import { useState } from 'react';

interface NavItem {
  href: string;
  title: string;
  label?: string;
  isProfile?: boolean;
}

function Navbar() {
  const [isOpen, setIsOpen] = useState(false);
  const { user, status, canManageRequests, isAdminUser, logout } = useAuth();
  const isLoggedIn = status === 'authenticated';

  const navItemsLeft: NavItem[] = [
    { href: '/home', title: 'Kezdőlap', label: 'Kezdőlap' },
    ...(canManageRequests ? [{ href: '/requesting', title: 'FoodEx kérés', label: 'FoodEx kérés' }] : []),
    ...(isAdminUser ? [{ href: '/config', title: 'Konfiguráció', label: 'Konfig' }] : []),
    ...(isAdminUser ? [{ href: '/clubs', title: 'Körök kezelése', label: 'Körök' }] : []),
    ...(isAdminUser ? [{ href: '/users', title: 'Felhasználók', label: 'Tagok' }] : []),
  ];

  const navItemsRight: NavItem[] = isLoggedIn
    ? [
        { href: '/openings', title: 'Nyitások', label: 'Nyitások' },
        ...(canManageRequests ? [{ href: '/requests', title: 'Kérések', label: 'Kérések' }] : []),
        { href: '/shifts', title: 'Műszakok', label: 'Műszakok' },
        { href: '/profile', title: 'Profil', isProfile: true },
      ]
    : [];

  const handleLogout = async (): Promise<void> => {
    setIsOpen(false);
    await logout();
  };

  return (
    <nav className='w-full bg-white border-b-2 border-[#332C81] px-4 py-2'>
      <div className='flex justify-between items-center'>
        <div className='text-[#332C81] text-2xl font-bold block sm:hidden'>FoodEx</div>

        <button className='sm:hidden text-[#332C81]' onClick={() => setIsOpen(!isOpen)}>
          {isOpen ? <X size={32} /> : <Menu size={32} />}
        </button>

        <div className='hidden sm:flex justify-between w-full'>
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

          <div className='flex space-x-4 items-center'>
            {status === 'loading' ? (
              <div className='text-gray-400 text-lg'>Ellenőrzés...</div>
            ) : !isLoggedIn ? (
              <a
                href={loginUrl}
                className='px-4 py-1 border-2 border-[#FF9860] bg-[#332C81] rounded-md text-white text-2xl font-semibold hover:bg-white hover:text-[#332C81] hover:border-[#332C81] transition-all'
              >
                Bejelentkezés
              </a>
            ) : (
              <>
                {navItemsRight.map((item) =>
                  item.isProfile ? (
                    <Link key={item.href} href={item.href} title={item.title}>
                      <Image
                        src='/profile.png'
                        alt={user?.nickname || 'Profil'}
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
                )}
                <button
                  type='button'
                  onClick={() => void handleLogout()}
                  className='px-3 py-1 border-2 border-[#332C81] rounded-md text-[#332C81] text-xl font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
                >
                  Kijelentkezés
                </button>
              </>
            )}
          </div>
        </div>
      </div>

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

          {status === 'loading' ? (
            <div className='text-gray-400 text-lg px-3 py-2'>Ellenőrzés...</div>
          ) : !isLoggedIn ? (
            <a
              href={loginUrl}
              className='px-3 py-2 text-center border-2 border-[#FF9860] bg-[#332C81] rounded-md text-white text-lg font-semibold'
            >
              Bejelentkezés
            </a>
          ) : (
            <>
              {navItemsRight.map((item) =>
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
              )}
              <button
                type='button'
                onClick={() => void handleLogout()}
                className='px-3 py-2 border-2 border-[#332C81] rounded-md text-[#332C81] text-lg font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
              >
                Kijelentkezés
              </button>
            </>
          )}
        </div>
      )}
    </nav>
  );
}

export default Navbar;
