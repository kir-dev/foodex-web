import { ShiftEntity } from './shift';
import { User } from './user';

export type HomePageData = {
  feelingOfTheWeek: string;
  foodExLogo: string; // Hozzáadva a backendből érkező logó URL miatt
  activeMembers: User[];
  upcomingOpenings?: any[]; // A backendről érkező pontos kulcs
  upcomingShifts?: ShiftEntity[]; // Megtartva a visszafelé kompatibilitás miatt
};
