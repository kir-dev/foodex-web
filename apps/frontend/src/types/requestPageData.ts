export interface DetailedOpeningRequestDto {
  id: number;
  isAccepted: boolean;
  user: {
    id: number;
    nickname: string;
    role: string;
  };
  cookingClub: {
    id: number;
    name: string;
  };
  opening: string;
  closing: string;
  place: string;
  description: string;
}
