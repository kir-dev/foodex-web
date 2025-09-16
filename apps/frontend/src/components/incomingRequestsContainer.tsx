'use client';
import { Shift, ShiftTable } from '@/components/ShiftTable';

type RequestsProps = {
  requests: Shift[];
};

export function IncomingRequestsContainer({ requests }: RequestsProps) {
  return (
    <ShiftTable shifts={requests} buttons={[{ label: 'Elfogadás', onClick: (s) => console.log('Elfogadás:', s) }]} />
  );
}
