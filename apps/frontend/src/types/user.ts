export type User = {
  id: number;
  role: 'admin' | 'member' | 'guest' | 'newbie';
  name: string;
  nickname: string;
  email: string;
  favoriteQuote: string;
  isActive: boolean;
  profilePicture: string;
};
