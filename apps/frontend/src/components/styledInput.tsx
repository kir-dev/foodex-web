import { HTMLInputTypeAttribute } from 'react';

type InputSize = 'full' | 'small' | 'medium' | 'large';

interface StyledInputProps {
  type: HTMLInputTypeAttribute;
  placeholder?: string;
  size?: InputSize;
}

export function StyledInput({ type, placeholder, size = 'full' }: StyledInputProps) {
  const sizeClasses: Record<InputSize, string> = {
    full: 'w-full',
    small: 'w-36',
    medium: 'w-40',
    large: 'w-44',
  };

  return (
    <input
      type={type}
      placeholder={placeholder}
      className={`bg-white p-2 rounded-2xl text-black text-xl mt-3 ${sizeClasses[size]}`}
    />
  );
}
