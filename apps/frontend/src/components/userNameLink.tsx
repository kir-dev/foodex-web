import Link from 'next/link';
import { ReactNode } from 'react';

type UserNameLinkProps = {
  userId: number;
  children: ReactNode;
  className?: string;
};

export function UserNameLink({ userId, children, className = '' }: UserNameLinkProps) {
  return (
    <Link href={`/profile/${userId}`} className={`hover:underline ${className}`}>
      {children}
    </Link>
  );
}
