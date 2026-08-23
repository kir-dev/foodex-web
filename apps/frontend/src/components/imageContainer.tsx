import React from 'react';

export function ImageContainer({ children }: { children: React.ReactNode }) {
  return (
    <div
      className='w-full max-w-[350px] self-center border-2 border-brand-fg rounded-xl bg-brand p-3
                 md:w-[350px] md:shrink-0 md:self-start'
    >
      <div className='overflow-hidden rounded-xl border-4 border-accent'>{children}</div>
    </div>
  );
}
