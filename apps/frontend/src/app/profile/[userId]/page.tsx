'use client';

import { useAuth } from '@/components/auth-provider';
import { PageState } from '@/components/page-state';
import { ProfileView } from '@/components/profileView';
import { RequireAuth } from '@/components/require-auth';
import { apiFetch, isApiError } from '@/lib/api';
import { DetailedUserDto } from '@/types/api';
import { useRefetchOnPath } from '@/lib/use-refetch-on-path';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function OtherProfilePage() {
  return (
    <RequireAuth loadingLabel='Profil betöltése...'>
      <OtherProfileContent />
    </RequireAuth>
  );
}

function OtherProfileContent() {
  const params = useParams<{ userId: string }>();
  const router = useRouter();
  const { user: me } = useAuth();
  const userId = Number(params.userId);
  const [user, setUser] = useState<DetailedUserDto | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (Number.isFinite(userId) && me && userId === me.id) {
      router.replace('/profile');
    }
  }, [me, router, userId]);

  useRefetchOnPath(async () => {
    if (!Number.isFinite(userId)) {
      setError('Érvénytelen felhasználó.');
      return;
    }
    if (me && userId === me.id) {
      return;
    }

    try {
      const data = await apiFetch<DetailedUserDto>(`/api/users/${userId}`);
      setUser(data);
    } catch (err) {
      setError(isApiError(err) ? err.message : 'Nem sikerült betölteni a profilt.');
    }
  });

  if (!Number.isFinite(userId) || error) {
    return <PageState variant='error'>{error ?? 'Érvénytelen felhasználó.'}</PageState>;
  }

  if (me && userId === me.id) {
    return <PageState>Profil betöltése...</PageState>;
  }

  if (!user) {
    return <PageState>Profil betöltése...</PageState>;
  }

  return <ProfileView user={user} editable={false} />;
}
