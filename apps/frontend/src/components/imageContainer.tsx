import React from 'react';

export function ImageContainer({ children }: { children: React.ReactNode }) {
  return (
    <div className='w-[300px] h-auto border-2 border-[#332C81] rounded-xl bg-[#332C81] flex items-center justify-center p-3'>
      <div className='border-4 border-[#FF9860] rounded-xl overflow-hidden max-w-[90%] max-h-[250px]'>{children}</div>
    </div>
  );
}
