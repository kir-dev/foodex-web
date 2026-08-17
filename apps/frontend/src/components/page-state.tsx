import { ReactNode } from 'react';

type PageStateProps = {
  children: ReactNode;
  variant?: 'info' | 'error';
};

export function PageState({ children, variant = 'info' }: PageStateProps) {
  return (
    <div
      className={`w-full flex-1 flex items-center justify-center bg-white text-xl font-semibold p-6 text-center ${
        variant === 'error' ? 'text-red-500' : 'text-[#332C81]'
      }`}
    >
      {children}
    </div>
  );
}
