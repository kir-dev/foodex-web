import Image from 'next/image';
import Link from 'next/link';

interface NavItem {
  href: string;
  title: string;
  label?: string;
  isProfile?: boolean;
}

function Navbar() {
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
    <nav className='flex justify-between items-center px-4 py-2 bg-white border-b border-gray-300'>
      {/* Bal oldal */}
      <div className='flex space-x-4'>
        {navItemsLeft.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            title={item.title}
            className='px-3 py-1 border border-[#332C81] rounded-md text-[#332C81]
                      text-xl font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
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
                className='rounded-full border border-[#332C81] hover:bg-[#332C81] transition-all p-1 hover:p-1.5'
              />
            </Link>
          ) : (
            <Link
              key={item.href}
              href={item.href}
              title={item.title}
              className='px-3 py-1 border border-[#332C81] rounded-md text-[#332C81]
                        text-xl font-semibold hover:bg-[#332C81] hover:text-[#FF9860] transition-all'
            >
              {item.label}
            </Link>
          )
        )}
      </div>
    </nav>
  );
}

export default Navbar;
