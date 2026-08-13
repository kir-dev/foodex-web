import { Shift } from '@/components/ShiftTable';
import { formatShortDate, formatTimeRange, formatWeekday } from '@/lib/dates';
import {
  canJoinShift,
  canLeaveShift,
  DetailedOpeningRequestDto,
  DetailedShiftDto,
  DetailedUserDto,
  isOnShift,
  memberCount,
  newbieCount,
} from '@/types/api';

export function shiftToRow(shift: DetailedShiftDto, user?: DetailedUserDto): Shift {
  const names = [...shift.members, ...shift.newbies].map((person) =>
    user && person.id === user.id ? `${person.nickname} (te)` : person.nickname
  );

  return {
    id: shift.id,
    groupName: shift.cookingClub?.name || `Kör #${shift.cookingClub?.id ?? shift.id}`,
    day: formatWeekday(shift.opening),
    time: formatTimeRange(shift.opening, shift.closing),
    location: shift.place,
    date: formatShortDate(shift.opening),
    occupancy:
      `${memberCount(shift)}/${shift.maxMembers} tag` + (newbieCount(shift) > 0 ? `, ${newbieCount(shift)} újonc` : ''),
    names: names.length > 0 ? names.join(', ') : 'Még senki',
    joined: user ? isOnShift(shift, user.id) : false,
    canJoin: user ? canJoinShift(user, shift) : false,
    canLeave: user ? canLeaveShift(user, shift) : false,
  };
}

export function requestToRow(request: DetailedOpeningRequestDto): Shift {
  return {
    id: request.id,
    groupName: request.cookingClub?.name || `Kör ID: ${request.cookingClub?.id ?? request.id}`,
    day: formatWeekday(request.opening),
    time: formatTimeRange(request.opening, request.closing),
    location: request.place,
    date: formatShortDate(request.opening),
  };
}
