export type User = {
  id: number;
  role: 'ADMIN' | 'MEMBER' | 'GUEST' | 'NEWBIE';
  name: string;
  nickname: string;
  email: string;
  favouriteQuote: string;
  isActive: boolean;
  profilePicture: string | null;
};
