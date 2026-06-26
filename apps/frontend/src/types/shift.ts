export interface ShiftEntity {
  id: number;
  cookingClubId?: number;
  cookingClub?: {
    id: number;
    name: string;
  };
  maxMembers: number;
  opening: string;
  closing: string;
  place: string;
  members: any[];
  newbies: any[];
  comment: string;
}
