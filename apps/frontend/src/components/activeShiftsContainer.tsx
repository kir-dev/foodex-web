'use client';
import { Shift, ShiftTable } from '@/components/ShiftTable';

type ShiftsProps = {
  shifts: Shift[];
};

export function ActiveShiftsContainer({ shifts }: ShiftsProps) {
  return (
    <ShiftTable shifts={shifts} buttons={[{ label: 'Jelentkezés', onClick: (s) => console.log('Jelentkezés:', s) }]} />
  );
}
