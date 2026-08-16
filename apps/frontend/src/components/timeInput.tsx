'use client';

const HOURS = Array.from({ length: 24 }, (_, hour) => String(hour).padStart(2, '0'));
const MINUTES = Array.from({ length: 60 }, (_, minute) => String(minute).padStart(2, '0'));

type TimeInputProps = {
  value: string;
  onChange: (value: string) => void;
  className?: string;
};

function parseTime(value: string): { hour: string; minute: string } {
  if (value.length >= 5 && value[2] === ':') {
    return { hour: value.slice(0, 2), minute: value.slice(3, 5) };
  }
  return { hour: '', minute: '' };
}

export function TimeInput({ value, onChange, className = '' }: TimeInputProps) {
  const { hour, minute } = parseTime(value);

  const emit = (nextHour: string, nextMinute: string): void => {
    if (!nextHour && !nextMinute) {
      onChange('');
      return;
    }
    onChange(`${nextHour || '00'}:${nextMinute || '00'}`);
  };

  return (
    <div className={`inline-flex items-center gap-1 ${className}`}>
      <select
        aria-label='Óra'
        className='rounded-2xl p-2 text-xl bg-white text-black'
        value={hour}
        onChange={(event) => emit(event.target.value, minute || '00')}
      >
        <option value=''>--</option>
        {HOURS.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
      <span className='text-xl font-semibold'>:</span>
      <select
        aria-label='Perc'
        className='rounded-2xl p-2 text-xl bg-white text-black'
        value={minute}
        onChange={(event) => emit(hour || '00', event.target.value)}
      >
        <option value=''>--</option>
        {MINUTES.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </div>
  );
}
