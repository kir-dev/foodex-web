'use client';

import { useAuth } from '@/components/auth-provider';
import { PageState } from '@/components/page-state';
import { ProfileView } from '@/components/profileView';
import { RequireAuth } from '@/components/require-auth';

export default function ProfilePage() {
  return (
    <RequireAuth loadingLabel='Profil betöltése...'>
      <OwnProfileContent />
    </RequireAuth>
  );
}

function OwnProfileContent() {
  const { user, refresh } = useAuth();

  if (!user) {
    return <PageState>Profil betöltése...</PageState>;
  }

  return <ProfileView user={user} editable onSaved={refresh} />;
}
