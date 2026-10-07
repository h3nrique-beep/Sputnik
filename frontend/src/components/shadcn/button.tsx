import { cva, type VariantProps } from "class-variance-authority";
import { type ButtonHTMLAttributes, forwardRef } from "react";
import { cn } from "@/lib/cn";

const buttonVariants = cva("ui-button", {
  variants: {
    variant: {
      default: "ui-button--primary",
      secondary: "ui-button--secondary",
      outline: "ui-button--secondary",
      ghost: "ui-button--ghost",
      link: "ui-button--link",
      destructive: "ui-button--danger",
    },
    size: {
      default: "",
      sm: "ui-button--sm",
      lg: "ui-button--lg",
      icon: "ui-button--icon",
    },
  },
  defaultVariants: { variant: "default", size: "default" },
});

export interface ButtonProps
  extends
    ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, ...props }, ref) => (
    <button
      className={cn(buttonVariants({ variant, size }), className)}
      ref={ref}
      {...props}
    />
  ),
);
Button.displayName = "Button";
