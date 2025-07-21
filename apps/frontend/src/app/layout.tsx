'use client';
import Navbar from '@/components/navbar';
import './globals.css';

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang='hu'>
      <body>
        <Navbar />
        {children}
      </body>
    </html>
  );
}
