import { AuthProvider } from '@/components/auth-provider';
import Footer from '@/components/footer';
import Navbar from '@/components/navbar';
import type { Metadata } from 'next';
import { ReactNode } from 'react';
import './globals.css';

export const metadata: Metadata = {
  title: 'FoodEx',
  description: 'FoodEx kör weboldal',
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang='hu'>
      <body className='min-h-screen flex flex-col'>
        <AuthProvider>
          <Navbar />
          <div className='flex-1 flex flex-col'>{children}</div>
          <Footer />
        </AuthProvider>
      </body>
    </html>
  );
}
