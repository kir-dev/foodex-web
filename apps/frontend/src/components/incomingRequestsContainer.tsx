'use client';

import { OpeningRequestRow, OpeningRequestTable } from '@/components/openingRequestTable';

type RequestsProps = {
  requests: OpeningRequestRow[];
  onAccept: (id: number) => void;
  onReject?: (id: number) => void;
  onEdit?: (id: number) => void;
};

export function IncomingRequestsContainer({ requests, onAccept, onReject, onEdit }: RequestsProps) {
  return (
    <OpeningRequestTable
      requests={requests}
      buttons={[
        {
          label: 'Elfogadás',
          onClick: (request) => onAccept(request.id),
        },
        ...(onEdit
          ? [
              {
                label: 'Módosítás',
                onClick: (request: OpeningRequestRow) => onEdit(request.id),
              },
            ]
          : []),
        ...(onReject
          ? [
              {
                label: 'Elutasítás',
                onClick: (request: OpeningRequestRow) => onReject(request.id),
              },
            ]
          : []),
      ]}
    />
  );
}
