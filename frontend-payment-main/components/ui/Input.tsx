import type { InputHTMLAttributes } from "react";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
};

export function Input({ label, className = "", id, ...props }: InputProps) {
  const inputId = id ?? props.name;
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-primary">
      {label}
      <input
        id={inputId}
        className={`rounded-lg border border-primary/15 bg-surface-muted px-3 py-2 text-primary placeholder:text-primary/40 focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/30 ${className}`}
        {...props}
      />
    </label>
  );
}
