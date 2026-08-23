'use client';

import { OpeningRequestRow, OpeningRequestTable } from '@/components/openingRequestTable';

type RequestsProps = {
  requests: OpeningRequestRow[];
  onEdit: (request: OpeningRequestRow) => void;
  onDelete: (request: OpeningRequestRow) => void;
};

export function ApprovedRequestsContainer({ requests, onEdit, onDelete }: RequestsProps) {
  return (
    <OpeningRequestTable
      requests={requests}
      buttons={[
        { label: 'Módosítás', onClick: onEdit },
        { label: 'Törlés', onClick: onDelete },
      ]}
    />
  );
}
