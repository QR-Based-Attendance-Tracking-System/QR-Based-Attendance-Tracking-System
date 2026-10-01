import type { InputHTMLAttributes, ReactNode } from "react";

type InputFieldProps = {
  label: string;
  icon: ReactNode;
} & InputHTMLAttributes<HTMLInputElement>;

export default function InputField({
  label,
  icon,
  className = "",
  ...props
}: InputFieldProps) {
  return (
    <label className="block w-full">
      <span className="mb-2 block text-[14px] font-semibold leading-none text-[#636161]">
        {label}
      </span>

      <span className="flex h-[46px] w-full items-center gap-3 rounded-[14px] bg-[#d4d4d4] px-4">
        <span className="flex shrink-0 items-center text-[#45240e]">
          {icon}
        </span>

        <input
          {...props}
          className={`min-w-0 flex-1 bg-transparent text-[13px] text-[#45240e] outline-none placeholder:text-[12px] placeholder:text-[#636161] ${className}`}
        />
      </span>
    </label>
  );
}