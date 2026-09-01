'use client';

import clsx from 'clsx';
import React, { ComponentPropsWithoutRef } from 'react';

export type ButtonVariant = 'primary' | 'secondary' | 'danger';

interface ButtonProps extends ComponentPropsWithoutRef<'button'> {
  label: string;
  variant?: ButtonVariant;
}

const Button: React.FC<ButtonProps> = ({ label, type = 'button', className = '', variant = 'primary', ...props }) => {
  const baseClasses = 'font-semibold text-xl px-4 py-2 rounded-full border-2 transition-all';

  const variantClasses = {
    primary: 'bg-brand-deep text-accent border-accent hover:bg-brand-deep-hover',
    secondary: 'bg-surface text-brand-fg border-brand-fg hover:bg-brand hover:text-accent',
    danger: 'bg-red-600 text-white border-red-700 hover:bg-red-700',
  };

  return (
    <button
      type={type}
      {...props}
      className={clsx(
        baseClasses,
        variantClasses[variant],
        'disabled:opacity-50 disabled:cursor-not-allowed',
        className
      )}
    >
      {label}
    </button>
  );
};

export default Button;
