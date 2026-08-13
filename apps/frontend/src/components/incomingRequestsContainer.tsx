'use client';

import { Shift, ShiftTable } from '@/components/ShiftTable';

type RequestsProps = {
  requests: Shift[];
  onAccept: (id: number) => void;
};

export function IncomingRequestsContainer({ requests, onAccept }: RequestsProps) {
  return (
    <ShiftTable
      shifts={requests}
      buttons={[
        {
          label: 'Elfogadás',
          onClick: (shift) => onAccept(shift.id),
        },
      ]}
    />
  );
}
