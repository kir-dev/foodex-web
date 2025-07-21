import React from 'react';

interface ButtonProps {
  label: string;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
  className?: string;
}

const Button: React.FC<ButtonProps> = ({ label, onClick, type = 'button', className = '' }) => {
  return (
    <button
      type={type}
      onClick={onClick}
      className={`bg-[#2f2173] text-[#ff9860] font-semibold text-xl px-4 py-2
                  rounded-full border-2 border-[#ff9860] hover:bg-[#3d2d91] transition-all ${className}`}
    >
      {label}
    </button>
  );
};

export default Button;
