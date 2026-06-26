import { ComponentPropsWithoutRef } from 'react';

type InputSize = 'full' | 'small' | 'medium' | 'large';

// Az Omit-tal kikapjuk az eredeti, szám alapú 'size' propot, hogy a sajátunkat tehessük a helyére
interface StyledInputProps extends Omit<ComponentPropsWithoutRef<'input'>, 'size'> {
  size?: InputSize;
}

export function StyledInput({ size = 'full', className = '', ...props }: StyledInputProps) {
  const sizeClasses: Record<InputSize, string> = {
    full: 'w-full',
    small: 'w-36',
    medium: 'w-40',
    large: 'w-44',
  };

  return (
    <input
      {...props} // Most már biztonságosan átmegy minden, a TS nem fog akadékoskodni
      className={`bg-white p-2 rounded-2xl text-black text-xl mt-2 ${sizeClasses[size]} ${className}`}
    />
  );
}
