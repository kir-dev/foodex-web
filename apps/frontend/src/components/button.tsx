import clsx from 'clsx';
import React from 'react';

export type ButtonVariant = 'primary' | 'secondary';

interface ButtonProps {
  label: string;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
  className?: string;
  variant?: ButtonVariant;
}

const Button: React.FC<ButtonProps> = ({ label, onClick, type = 'button', className = '', variant = 'primary' }) => {
  const baseClasses = 'font-semibold text-xl px-4 py-2 rounded-full border-2 transition-all';

  const variantClasses = {
    primary: 'bg-[#2f2173] text-[#ff9860] border-[#ff9860] hover:bg-[#3d2d91]',
    secondary: 'bg-white text-[#2f2173] border-[#2f2173] hover:bg-[#2f2173] hover:text-[#ff9860]',
  };

  return (
    <button type={type} onClick={onClick} className={clsx(baseClasses, variantClasses[variant], className)}>
      {label}
    </button>
  );
};

export default Button;
