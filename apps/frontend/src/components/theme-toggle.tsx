'use client';

import { useTheme } from '@/components/theme-provider';
import { Moon, Sun } from 'lucide-react';
import { useEffect, useState } from 'react';

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const isDark = theme === 'dark';

  return (
    <button
      type='button'
      onClick={toggleTheme}
      aria-label={isDark ? 'Világos mód' : 'Sötét mód'}
      title={isDark ? 'Világos mód' : 'Sötét mód'}
      className='flex items-center justify-center p-2 border-2 border-brand-fg rounded-md text-brand-fg hover:bg-brand hover:text-accent transition-all'
    >
      {mounted ? isDark ? <Sun size={22} /> : <Moon size={22} /> : <span className='inline-block h-[22px] w-[22px]' />}
    </button>
  );
}
