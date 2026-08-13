'use client';

import { Shift, ShiftTable } from '@/components/ShiftTable';

type RequestsProps = {
  requests: Shift[];
  onAccept: (id: number) => void;
  onReject?: (id: number) => void;
  onEdit?: (id: number) => void;
};

export function IncomingRequestsContainer({ requests, onAccept, onReject, onEdit }: RequestsProps) {
  return (
    <ShiftTable
      shifts={requests}
      buttons={[
        {
          label: 'Elfogadás',
          onClick: (shift) => onAccept(shift.id),
        },
        ...(onEdit
          ? [
              {
                label: 'Módosítás',
                onClick: (shift: Shift) => onEdit(shift.id),
              },
            ]
          : []),
        ...(onReject
          ? [
              {
                label: 'Elutasítás',
                onClick: (shift: Shift) => onReject(shift.id),
              },
            ]
          : []),
      ]}
    />
  );
}
