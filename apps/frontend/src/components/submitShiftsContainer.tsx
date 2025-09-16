'use client';
import { Shift, ShiftTable } from '@/components/ShiftTable';

type ShiftsProps = {
  shifts: Shift[];
};

export function SubmitShiftsContainer({ shifts }: ShiftsProps) {
  return <ShiftTable shifts={shifts} buttons={[{ label: 'Leadás', onClick: (s) => console.log('Leadás:', s) }]} />;
}
