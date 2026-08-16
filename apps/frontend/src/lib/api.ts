import { apiUrl } from '@/lib/config';

export class ApiError extends Error {
  readonly status: number;
  readonly body: string;

  constructor(status: number, body: string) {
    super(messageFromBody(status, body));
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

type ApiFetchOptions = Omit<RequestInit, 'body'> & {
  body?: unknown;
  parseJson?: boolean;
};

let csrfToken: string | null = null;

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}

export function resolveApiUrl(path: string): string {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  return `${apiUrl}${path.startsWith('/') ? path : `/${path}`}`;
}

export async function apiFetch<T>(path: string, options: ApiFetchOptions = {}): Promise<T> {
  const { body, parseJson, headers: initHeaders, method: initMethod, ...requestInit } = options;
  const method = (initMethod ?? 'GET').toUpperCase();
  const headers = new Headers(initHeaders);
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }

  if (body !== undefined && !isFormData(body)) {
    headers.set('Content-Type', 'application/json');
  }

  if (isMutatingMethod(method)) {
    const token = await ensureCsrfToken();
    if (token) {
      headers.set('X-XSRF-TOKEN', token);
    }
  }

  const response = await fetch(resolveApiUrl(path), {
    ...requestInit,
    method,
    cache: 'no-store',
    credentials: 'include',
    headers,
    body: serializeBody(body),
  });

  captureCsrfFromResponse(response);

  if (response.type === 'opaqueredirect' || response.status === 302 || response.status === 303) {
    return undefined as T;
  }

  if (!response.ok) {
    const text = await response.text();
    throw new ApiError(response.status, text);
  }

  if (parseJson === false || response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (!contentType.includes('application/json')) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

function isMutatingMethod(method: string): boolean {
  return method === 'POST' || method === 'PUT' || method === 'PATCH' || method === 'DELETE';
}

function isFormData(value: unknown): value is FormData {
  return typeof FormData !== 'undefined' && value instanceof FormData;
}

function serializeBody(body: unknown): BodyInit | undefined {
  if (body === undefined) {
    return undefined;
  }
  if (typeof body === 'string' || isFormData(body) || body instanceof Blob) {
    return body;
  }
  return JSON.stringify(body);
}

function readCsrfCookie(): string | null {
  if (typeof document === 'undefined') {
    return null;
  }
  const parts = `; ${document.cookie}`.split('; XSRF-TOKEN=');
  if (parts.length < 2) {
    return null;
  }
  const value = parts.pop()?.split(';').shift();
  return value ? decodeURIComponent(value) : null;
}

function captureCsrfFromResponse(response: Response): void {
  const header = response.headers.get('X-XSRF-TOKEN') ?? response.headers.get('X-Xsrf-Token');
  if (header) {
    csrfToken = header;
  }
}

async function ensureCsrfToken(): Promise<string | null> {
  if (csrfToken) {
    return csrfToken;
  }

  const fromCookie = readCsrfCookie();
  if (fromCookie) {
    csrfToken = fromCookie;
    return csrfToken;
  }

  const response = await fetch(resolveApiUrl('/api/homepage'), {
    credentials: 'include',
    headers: { Accept: 'application/json' },
  });
  captureCsrfFromResponse(response);
  if (!csrfToken) {
    csrfToken = readCsrfCookie();
  }
  return csrfToken;
}

function messageFromBody(status: number, body: string): string {
  if (!body) {
    return fallbackStatusMessage(status);
  }
  try {
    const json = JSON.parse(body) as {
      detail?: string;
      message?: string;
      title?: string;
      error?: string;
    };
    return json.detail || json.message || json.title || json.error || fallbackStatusMessage(status);
  } catch {
    return body;
  }
}

function fallbackStatusMessage(status: number): string {
  if (status === 409) {
    return 'A művelet ütközik a jelenlegi állapottal.';
  }
  return `HTTP ${status}`;
}

export function shiftActionErrorMessage(error: unknown, action: 'join' | 'leave'): string {
  if (!isApiError(error)) {
    return action === 'join' ? 'Nem sikerült jelentkezni a műszakra.' : 'Nem sikerült leadni a műszakot.';
  }

  const raw = error.message.toLowerCase();
  if (raw.includes('already added')) {
    return 'Már jelentkeztél erre a műszakra.';
  }
  if (raw.includes('capacity full')) {
    return 'Erre a műszakra a te szerepeddel már nincs szabad hely.';
  }
  if (raw.includes('not part of shift')) {
    return 'Nem vagy jelentkezve erre a műszakra.';
  }
  if (raw.includes('guests cannot')) {
    return 'Vendégként nem jelentkezhetsz műszakra.';
  }
  if (raw.includes('already started')) {
    return 'Folyamatban lévő műszakra nem lehet jelentkezni.';
  }
  if (error.status === 409 || raw === 'conflict') {
    return action === 'join'
      ? 'Nem sikerült jelentkezni: már fel vagy véve, vagy nincs hely a szerepednek.'
      : 'Nem sikerült leadni: nem vagy ezen a műszakon.';
  }
  return error.message;
}
