'use client';

import { Shift, ShiftTable } from '@/components/ShiftTable';

type ShiftsProps = {
  shifts: Shift[];
  onJoin?: (shift: Shift) => void;
  onLeave?: (shift: Shift) => void;
  emptyLabel?: string;
};

export function ActiveShiftsContainer({ shifts, onJoin, onLeave, emptyLabel }: ShiftsProps) {
  return (
    <ShiftTable
      shifts={shifts}
      showNamesColumn
      emptyLabel={emptyLabel}
      buttons={[
        {
          label: 'Jelentkezés',
          onClick: (shift) => onJoin?.(shift),
          hidden: (shift) => !onJoin || !shift.canJoin,
        },
        {
          label: 'Leadás',
          onClick: (shift) => onLeave?.(shift),
          hidden: (shift) => !onLeave || !shift.canLeave,
        },
      ]}
    />
  );
}
