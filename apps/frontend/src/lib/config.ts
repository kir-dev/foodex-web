const DEFAULT_API_URL = 'https://api.foodex.sch.bme.hu';

function trimTrailingSlash(value: string): string {
  return value.endsWith('/') ? value.slice(0, -1) : value;
}

export const apiUrl: string = trimTrailingSlash(process.env.NEXT_PUBLIC_API_URL?.trim() || DEFAULT_API_URL);

export const loginUrl: string = `${apiUrl}/oauth2/authorization/authsch`;

export const logoutUrl: string = `${apiUrl}/logout`;
