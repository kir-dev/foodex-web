import { Shift } from '@/components/ShiftTable';
import { formatShortDate, formatTimeRange, formatWeekday } from '@/lib/dates';
import {
  canJoinShift,
  canLeaveShift,
  DetailedShiftDto,
  DetailedUserDto,
  isOnShift,
  memberCount,
  newbieCount,
  shiftWorkers,
  trialCount,
} from '@/types/api';

export function shiftOccupancyLabel(shift: DetailedShiftDto): string {
  const members = memberCount(shift);
  const newbies = newbieCount(shift);
  const trials = trialCount(shift);
  return `${members + newbies}/${shift.maxMembers} tag(${members}) újonc(${newbies}) próbás(${trials})`;
}

export function shiftToRow(shift: DetailedShiftDto, user?: DetailedUserDto): Shift {
  const workers = shiftWorkers(shift).map((person) => ({
    id: person.id,
    nickname: user && person.id === user.id ? `${person.nickname} (te)` : person.nickname,
  }));

  return {
    id: shift.id,
    groupName: shift.cookingClub?.name || `Kör #${shift.cookingClub?.id ?? shift.id}`,
    day: formatWeekday(shift.opening),
    time: formatTimeRange(shift.opening, shift.closing),
    location: shift.place,
    date: formatShortDate(shift.opening),
    occupancy: shiftOccupancyLabel(shift),
    workers,
    joined: user ? isOnShift(shift, user.id) : false,
    canJoin: user ? canJoinShift(user, shift) : false,
    canLeave: user ? canLeaveShift(user, shift) : false,
  };
}
