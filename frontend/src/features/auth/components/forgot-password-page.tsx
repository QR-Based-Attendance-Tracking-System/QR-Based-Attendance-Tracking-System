"use client";

import Link from "next/link";
import AuthLayout from "@/components/AuthLayout";

export default function ForgotPasswordPage() {
  return (
    <AuthLayout>
      <section className="mt-7 w-full max-w-[370px] rounded-[30px] border-2 border-[#f8d929] bg-[#fcf5e0] px-8 py-8 text-center shadow-[0_5px_8px_rgba(0,0,0,0.25)] lg:mt-0 lg:max-w-[430px] lg:px-10 lg:py-10">
        <h1 className="text-[27px] font-bold text-[#45240e] lg:text-[30px]">Forgot Password</h1>
        <p className="mt-5 text-sm leading-6 text-[#636161]">Password recovery is managed by your faculty. Contact your account administrator to reset your password.</p>
        <Link href="/" className="mx-auto mt-6 block w-fit rounded-[9px] bg-[#45240e] px-7 py-3 text-[13px] font-medium text-white shadow">Back to Login</Link>
      </section>
    </AuthLayout>
  );
}
