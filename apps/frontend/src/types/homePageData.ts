import { ShiftEntity } from './shift';
import { User } from './user';

export type HomePageData = {
  feelingOfTheWeek: string;
  activeMembers: User[];
  upcomingShifts: ShiftEntity[];
};
