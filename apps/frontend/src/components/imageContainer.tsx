import React from 'react';
export function ImageContainer({ children }: { children: React.ReactNode }) {
  return (
    <div
      className='w-[350px] h-auto border-2 border-brand-fg rounded-xl bg-brand
                    flex items-center justify-center p-3'
    >
      <div className='border-4 border-accent rounded-xl overflow-hidden max-w-[90%] max-h-[250px]'>{children}</div>
    </div>
  );
}
