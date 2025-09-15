import Image from 'next/image';
import Link from 'next/link';

import { Menu, X } from 'lucide-react';
import { useState } from 'react';

interface NavItem {
  href: string;
  title: string;
  label?: string;
  isProfile?: boolean;
}

function Navbar() {
  const [isOpen, setIsOpen] = useState(false);

  const navItemsLeft: NavItem[] = [
    { href: '/home', title: 'Kezdőlap', label: 'Kezdőlap' },
    { href: '/requesting', title: 'FoodEx kérés', label: 'FoodEx kérés' },
  ];

  const navItemsRight: NavItem[] = [
    { href: '/openings', title: 'Nyitások', label: 'Nyitások' },
    { href: '/requests', title: 'Kérések', label: 'Kérések' },
    { href: '/shifts', title: 'Műszakok', label: 'Műszakok' },
    { href: '/profile', title: 'Profil', isProfile: true },
  ];

  return (
    <nav className='w-full bg-white border-b-2 border-[#332C81] px-4 py-2'>
      {/* Felső sor */}
      <div className='flex justify-between items-center'>
        {/* Bal oldal mobilon FoodEx felirat, ne legyen üres xd*/}
        <div className='text-[#332C81] text-2xl font-bold block sm:hidden'>FoodEx</div>

        {/* Hamburger ikon mobilra */}
        <button className='sm:hidden text-[#332C81]' onClick={() => setIsOpen(!isOpen)}>
          {isOpen ? <X size={32} /> : <Menu size={32} />}
        </button>

        {/* Normál menü nagyobb képernyőn */}
        <div className='hidden sm:flex justify-between w-full'>
          {/* Bal oldal */}
          <div className='flex space-x-4'>
            {navItemsLeft.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                title={item.title}
                className='px-3 py-1 border-2 border-[#332C81] rounded-md text-[#332C81]
                text-2xl font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
              >
                {item.label}
              </Link>
            ))}
          </div>

          {/* Jobb oldal */}
          <div className='flex space-x-4 items-center'>
            {navItemsRight.map((item) =>
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
                  className='px-3 py-1 border-2 border-[#332C81] rounded-md text-[#332C81]
                  text-2xl font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
                >
                  {item.label}
                </Link>
              )
            )}
          </div>
        </div>
      </div>

      {/* Mobil menü lenyíló */}
      {isOpen && (
        <div className='sm:hidden mt-2 flex flex-col space-y-2'>
          {[...navItemsLeft, ...navItemsRight].map((item) =>
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
        </div>
      )}
    </nav>
  );
}

export default Navbar;
