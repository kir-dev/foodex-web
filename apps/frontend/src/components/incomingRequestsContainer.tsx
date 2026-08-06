'use client';
import { Shift, ShiftTable } from '@/components/ShiftTable';

type ExtendedShift = Shift & { id?: number };

type RequestsProps = {
  requests: ExtendedShift[];
  onAccept: (id: number) => void;
};

export function IncomingRequestsContainer({ requests, onAccept }: RequestsProps) {
  return (
    <ShiftTable
      shifts={requests}
      buttons={[
        {
          label: 'Elfogadás',
          onClick: (s: ExtendedShift) => {
            if (s.id !== undefined) {
              onAccept(s.id);
            }
          },
        },
      ]}
    />
  );
}
