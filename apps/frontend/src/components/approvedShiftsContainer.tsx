'use client';

import { Shift, ShiftTable } from '@/components/ShiftTable';

type ShiftsProps = {
  shifts: Shift[];
  onEdit: (shift: Shift) => void;
  onDelete: (shift: Shift) => void;
};

export function ApprovedShiftsContainer({ shifts, onEdit, onDelete }: ShiftsProps) {
  return (
    <ShiftTable
      shifts={shifts}
      buttons={[
        { label: 'Módosítás', onClick: onEdit },
        { label: 'Törlés', onClick: onDelete },
      ]}
    />
  );
}
