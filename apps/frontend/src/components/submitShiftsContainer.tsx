'use client';

import { ActiveShiftsContainer } from '@/components/activeShiftsContainer';
import { Shift } from '@/components/ShiftTable';

type ShiftsProps = {
  shifts: Shift[];
  onJoin?: (shift: Shift) => void;
  onLeave?: (shift: Shift) => void;
  emptyLabel?: string;
};

export function SubmitShiftsContainer({ shifts, onJoin, onLeave, emptyLabel }: ShiftsProps) {
  return <ActiveShiftsContainer shifts={shifts} onJoin={onJoin} onLeave={onLeave} emptyLabel={emptyLabel} />;
}
