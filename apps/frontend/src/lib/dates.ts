export function parseLocalDateTime(value: string): Date {
  return new Date(value);
}

export function formatWeekday(value: string): string {
  const formatted = parseLocalDateTime(value).toLocaleDateString('hu-HU', { weekday: 'long' });
  if (!formatted) {
    return value;
  }
  return formatted.charAt(0).toUpperCase() + formatted.slice(1);
}

export function formatTime(value: string): string {
  return parseLocalDateTime(value).toLocaleTimeString('hu-HU', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatTimeRange(start: string, end: string): string {
  return `${formatTime(start)} - ${formatTime(end)}`;
}

export function formatShortDate(value: string): string {
  const date = parseLocalDateTime(value);
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${month}-${day}`;
}

export function formatLongDate(value: string): string {
  return parseLocalDateTime(value).toLocaleDateString('hu-HU', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function toLocalDateTimePayload(date: string, time: string): string {
  const normalized = time.length === 5 ? `${time}:00` : time;
  return `${date}T${normalized}`;
}

export function toDateInputValue(value: string): string {
  return value.slice(0, 10);
}

export function toTimeInputValue(value: string): string {
  if (value.length < 16) {
    return '';
  }
  return value.slice(11, 16);
}

export function shiftCountFromRange(opening: string, closing: string, defaultShiftHours = 2): number {
  const start = parseLocalDateTime(opening).getTime();
  const end = parseLocalDateTime(closing).getTime();
  const totalHours = (end - start) / (1000 * 60 * 60);
  return Math.max(1, Math.round(totalHours / defaultShiftHours));
}

/** Same overlap rule as ShiftRepository.findOverlappingSemester: closing > start AND opening < end. */
export function isWithinSemester(opening: string, closing: string, start: string, end: string): boolean {
  const openingTime = parseLocalDateTime(opening).getTime();
  const closingTime = parseLocalDateTime(closing).getTime();
  const startTime = parseLocalDateTime(start).getTime();
  const endTime = parseLocalDateTime(end).getTime();
  return closingTime > startTime && openingTime < endTime;
}

export function compareByOpeningDesc(
  a: { id: number; opening: string },
  b: { id: number; opening: string }
): number {
  const byDate = parseLocalDateTime(b.opening).getTime() - parseLocalDateTime(a.opening).getTime();
  if (byDate !== 0) {
    return byDate;
  }
  return b.id - a.id;
}
