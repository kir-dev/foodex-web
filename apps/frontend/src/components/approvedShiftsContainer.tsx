'use client';
import { Shift, ShiftTable } from '@/components/ShiftTable';

type ShiftsProps = {
  shifts: Shift[];
};

export function ApprovedShiftsContainer({ shifts }: ShiftsProps) {
  return (
    <ShiftTable
      shifts={shifts}
      buttons={[
        { label: 'Módosítás', onClick: (s) => console.log('Módosít:', s) },
        { label: 'Törlés', onClick: (s) => console.log('Törlés:', s) },
      ]}
    />
  );
}
