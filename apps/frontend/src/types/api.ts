export type Role = 'GUEST' | 'TRIAL' | 'NEWBIE' | 'MEMBER' | 'ADMIN' | 'SUPERUSER';

export const ROLE_LABEL: Record<Role, string> = {
  GUEST: 'vendég',
  TRIAL: 'próbás',
  NEWBIE: 'újonc',
  MEMBER: 'tag',
  ADMIN: 'admin',
  SUPERUSER: 'szuperadmin',
};

export type UserDto = {
  id: number;
  role: Role;
  nickname: string;
};

export type CookingClubDto = {
  id: number;
  name: string;
};

export type ShiftDto = {
  id: number;
  cookingClubId: number;
  maxMembers: number;
  opening: string;
  closing: string;
  place: string;
  comment: string;
  openingRequestId?: number | null;
  applicationOpening?: string | null;
};

export type OpeningRequestDto = {
  id: number;
  // Jackson serializes Kotlin `isAccepted` as `accepted`; OpenAPI still documents `isAccepted`.
  accepted?: boolean;
  isAccepted?: boolean;
  userId: number;
  cookingClubId: number;
  opening: string;
  closing: string;
  place: string;
  description: string;
};

export type DetailedUserDto = {
  id: number;
  role: Role;
  name: string;
  nickname: string | null;
  email: string;
  favouriteQuote: string | null;
  // Same Kotlin `is*` naming: runtime JSON uses `active`.
  active?: boolean;
  isActive?: boolean;
  profilePicture: string | null;
  leaderAt: CookingClubDto[];
  shifts: ShiftDto[];
  requests: OpeningRequestDto[];
};

export type UpdateUserDto = {
  name?: string | null;
  nickname?: string | null;
  email?: string | null;
  favouriteQuote?: string | null;
  profilePicture?: string | null;
};

export type DetailedCookingClubDto = {
  id: number;
  name: string;
  leaders: UserDto[];
  shifts: ShiftDto[];
  requests: OpeningRequestDto[];
};

export type DetailedOpeningRequestDto = {
  id: number;
  accepted?: boolean;
  isAccepted?: boolean;
  user: UserDto;
  cookingClub: CookingClubDto;
  opening: string;
  closing: string;
  place: string;
  description: string;
};

export type CreateOpeningRequestDto = {
  cookingClubId: number;
  opening: string;
  closing: string;
  place: string;
  description: string;
};

export type DetailedShiftDto = {
  id: number;
  cookingClub: CookingClubDto;
  maxMembers: number;
  opening: string;
  closing: string;
  place: string;
  comment: string;
  openingRequestId?: number | null;
  applicationOpening?: string | null;
  members: UserDto[];
  newbies: UserDto[];
  trials: UserDto[];
};

export type ActiveAndFullShifts = {
  activeShifts: DetailedShiftDto[];
  fullShifts: DetailedShiftDto[];
  notYetOpenShifts: DetailedShiftDto[];
};

export type HomepageDto = {
  feelingOfTheWeek: string;
  foodExLogo: string;
  homepageDescription: string;
  activeMembers: UserDto[];
  upcomingOpenings: DetailedOpeningRequestDto[];
};

export type ConfigurationDto = {
  feelingOfTheWeek: string;
  foodExLogo: string;
  homepageDescription: string;
  startOfSemester: string;
  endOfSemester: string;
};

export type UpdateConfigurationDto = {
  feelingOfTheWeek?: string;
  foodExLogo?: string;
  homepageDescription?: string;
  startOfSemester?: string;
  endOfSemester?: string;
};

export type CreateShiftDto = {
  cookingClubId: number;
  openingRequestId: number;
  maxMembers: number;
  opening: string;
  closing: string;
  place: string;
  comment?: string;
  applicationOpening?: string | null;
};

export type CreateCookingClubDto = {
  id: number;
  name: string;
};

export type UpdateCookingClubDto = {
  id: number;
  name: string;
};

export type TrialGrantDto = {
  id: number;
  name: string;
  internalId: string;
};

export type CreateTrialGrantDto = {
  name: string;
  internalId: string;
};

export type UpdateTrialGrantDto = {
  name: string;
  internalId: string;
};

export type AdminGrantDto = {
  id: number;
  name: string;
  internalId: string;
};

export type CreateAdminGrantDto = {
  name: string;
  internalId: string;
};

export type UpdateAdminGrantDto = {
  name: string;
  internalId: string;
};

export type UpdateOpeningRequestDto = {
  opening?: string;
  closing?: string;
  place?: string;
  description?: string;
};

export type CreateShiftFromOpeningRequestDto = {
  maxMembers: number;
  numberOfShifts: number;
  applicationOpening: string;
};

export type UpdateShiftDto = {
  cookingClubId?: number;
  maxMembers?: number;
  opening?: string;
  closing?: string;
  place?: string;
  comment?: string;
  applicationOpening?: string;
};

export function isAdmin(user: { role: Role }): boolean {
  return user.role === 'ADMIN' || user.role === 'SUPERUSER';
}

export function isSuperuser(user: { role: Role }): boolean {
  return user.role === 'SUPERUSER';
}

export function isClubLeaderOrAdmin(user: DetailedUserDto): boolean {
  return isAdmin(user) || user.leaderAt.length > 0;
}

export function canJoinShifts(user: DetailedUserDto): boolean {
  return user.role !== 'GUEST';
}

export function shiftWorkers(shift: DetailedShiftDto): UserDto[] {
  return [...shift.members, ...shift.newbies, ...(shift.trials ?? [])];
}

export function isOnShift(shift: DetailedShiftDto, userId: number): boolean {
  return shiftWorkers(shift).some((worker) => worker.id === userId);
}

export function memberCount(shift: DetailedShiftDto): number {
  return shift.members.length;
}

export function newbieCount(shift: DetailedShiftDto): number {
  return shift.newbies.length;
}

export function trialCount(shift: DetailedShiftDto): number {
  return (shift.trials ?? []).length;
}

/** Mirrors ShiftService.canJoin, plus "already signed up" / already started. */
export function canJoinShift(user: DetailedUserDto, shift: DetailedShiftDto): boolean {
  if (user.role === 'GUEST') {
    return false;
  }
  if (isOnShift(shift, user.id)) {
    return false;
  }
  if (new Date(shift.opening).getTime() <= Date.now()) {
    return false;
  }
  if (shift.applicationOpening && new Date(shift.applicationOpening).getTime() > Date.now()) {
    return false;
  }
  if (user.role === 'TRIAL') {
    return trialCount(shift) < memberCount(shift);
  }
  return memberCount(shift) + newbieCount(shift) < shift.maxMembers;
}

export function canLeaveShift(user: DetailedUserDto, shift: DetailedShiftDto): boolean {
  return isOnShift(shift, user.id);
}
