export type Role = 'ADMIN' | 'MEMBER' | 'NEWBIE' | 'GUEST';

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
  members: UserDto[];
  newbies: UserDto[];
};

export type ActiveAndFullShifts = {
  activeShifts: DetailedShiftDto[];
  fullShifts: DetailedShiftDto[];
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
  maxMembers: number;
  opening: string;
  closing: string;
  place: string;
  comment?: string;
};

export type CreateCookingClubDto = {
  id: number;
  name: string;
};

export type UpdateCookingClubDto = {
  id: number;
  name: string;
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
};

export type UpdateShiftDto = {
  cookingClubId?: number;
  maxMembers?: number;
  opening?: string;
  closing?: string;
  place?: string;
  comment?: string;
};

export function isAdmin(user: DetailedUserDto): boolean {
  return user.role === 'ADMIN';
}

export function isClubLeaderOrAdmin(user: DetailedUserDto): boolean {
  return user.role === 'ADMIN' || user.leaderAt.length > 0;
}

export function canJoinShifts(user: DetailedUserDto): boolean {
  return user.role !== 'GUEST';
}

export function isOnShift(shift: DetailedShiftDto, userId: number): boolean {
  return shift.members.some((member) => member.id === userId) || shift.newbies.some((newbie) => newbie.id === userId);
}

export function memberCount(shift: DetailedShiftDto): number {
  return shift.members.length;
}

export function newbieCount(shift: DetailedShiftDto): number {
  return shift.newbies.length;
}

/** Mirrors ShiftService.canJoin, plus "already signed up" / already closed. */
export function canJoinShift(user: DetailedUserDto, shift: DetailedShiftDto): boolean {
  if (user.role === 'GUEST') {
    return false;
  }
  if (isOnShift(shift, user.id)) {
    return false;
  }
  if (new Date(shift.closing).getTime() <= Date.now()) {
    return false;
  }
  if (user.role === 'NEWBIE') {
    const members = memberCount(shift);
    return members > 0 && newbieCount(shift) < members;
  }
  return memberCount(shift) < shift.maxMembers;
}

export function canLeaveShift(user: DetailedUserDto, shift: DetailedShiftDto): boolean {
  return isOnShift(shift, user.id);
}
