'use client';

import Button from '@/components/button';
import { apiFetch, isApiError } from '@/lib/api';
import { formatShortDate, formatTimeRange, formatWeekday } from '@/lib/dates';
import { shiftOccupancyLabel } from '@/lib/shift-view';
import { DetailedShiftDto } from '@/types/api';
import { useEffect, useState } from 'react';

type RequestShiftsModalProps = {
  requestId: number;
  clubName?: string;
  onClose: () => void;
};

export function RequestShiftsModal({ requestId, clubName, onClose }: RequestShiftsModalProps) {
  const [shifts, setShifts] = useState<DetailedShiftDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async (): Promise<void> => {
      setLoading(true);
      try {
        const data = await apiFetch<DetailedShiftDto[]>(`/api/requests/${requestId}/shifts`);
        setShifts(Array.isArray(data) ? data : []);
        setError(null);
      } catch (err) {
        setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a műszakokat.');
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [requestId]);

  return (
    <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50'>
      <div className='bg-white rounded-xl border-2 border-[#332C81] p-6 max-w-lg w-full space-y-4 shadow-xl max-h-[90vh] overflow-y-auto'>
        <h4 className='text-2xl font-bold text-[#332C81]'>Műszakok</h4>
        {clubName && <p className='text-gray-600 font-medium'>{clubName}</p>}

        {loading ? (
          <p className='text-gray-500 italic'>Műszakok betöltése...</p>
        ) : error ? (
          <p className='text-red-500 font-medium'>{error}</p>
        ) : shifts.length === 0 ? (
          <p className='text-gray-500 italic'>Nincsenek műszakok.</p>
        ) : (
          <ul className='flex flex-col gap-2'>
            {shifts.map((shift) => (
              <li
                key={shift.id}
                className='border-2 border-[#332C81] rounded-xl px-3 py-2 text-[#332C81]'
              >
                <span className='block font-semibold'>
                  {formatWeekday(shift.opening)}{' '}
                  <span className='font-medium text-gray-600'>{formatShortDate(shift.opening)}</span>
                </span>
                <span className='block'>{formatTimeRange(shift.opening, shift.closing)}</span>
                <span className='block italic text-gray-600'>{shift.place}</span>
                <span className='block font-medium'>{shiftOccupancyLabel(shift)}</span>
              </li>
            ))}
          </ul>
        )}

        <div className='flex justify-end pt-2'>
          <Button label='Bezárás' variant='secondary' onClick={onClose} />
        </div>
      </div>
    </div>
  );
}
