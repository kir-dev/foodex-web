import { AuthProvider } from '@/components/auth-provider';
import Footer from '@/components/footer';
import Navbar from '@/components/navbar';
import { ThemeProvider } from '@/components/theme-provider';
import type { Metadata } from 'next';
import { ReactNode } from 'react';
import './globals.css';

export const metadata: Metadata = {
  title: 'FoodEx',
  description: 'FoodEx kör weboldal',
  icons: {
    icon: { url: '/foodex-icon.png', sizes: '512x512', type: 'image/png' },
  },
};

const themeInitScript = `(function(){try{var s=localStorage.getItem('foodex-theme');var d=s==='dark'||(s!=='light'&&window.matchMedia('(prefers-color-scheme: dark)').matches);if(d)document.documentElement.classList.add('dark');}catch(e){}})();`;

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang='hu' suppressHydrationWarning>
      <body className='min-h-screen flex flex-col'>
        <script dangerouslySetInnerHTML={{ __html: themeInitScript }} />
        <ThemeProvider>
          <AuthProvider>
            <Navbar />
            <div className='flex-1 flex flex-col'>{children}</div>
            <Footer />
          </AuthProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
