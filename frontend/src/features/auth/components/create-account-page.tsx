"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { register } from "@/features/auth/services/auth-client";
import Link from "next/link";
import AuthLayout from "@/components/AuthLayout";

type AccountType = "student" | "lecturer";
const inputClass = "h-[48px] w-full rounded-[11px] bg-[#d4d4d4] px-4 text-[14px] text-[#45240e] outline-none placeholder:text-[#777] focus:ring-2 focus:ring-[#45240e]";
const labelClass = "mb-2 mt-4 block text-[13px] text-[#636161]";

export default function CreateAccountPage({ role = "student" }: { role?: AccountType }) {
  const router = useRouter();
  const accountType = role;
  const [message, setMessage] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    const form = new FormData(event.currentTarget);
    const details = {
      fullName: String(form.get("fullName") || "").trim(),
      username: String(form.get(accountType === "student" ? "studentId" : "lecturerUsername") || "").trim(),
      email: String(form.get("email") || "").trim(),
      password: String(form.get("password") || ""),
      confirmPassword: String(form.get("confirmPassword") || ""),
      institutionalId: String(form.get(accountType === "student" ? "studentId" : "lecturerId") || "").trim(),
      registrationCode: String(form.get("registrationCode") || ""),
    };
    void register(accountType, details).then(() => router.replace("/dashboard")).catch((error: unknown) => {
      setMessage(error instanceof Error ? error.message : "Account creation failed.");
    });
  }

  return (
    <AuthLayout>
      <section className="mt-7 w-full max-w-[370px] rounded-[30px] border-2 border-[#f8d929] bg-[#fcf5e0] px-8 py-8 shadow-[0_5px_8px_rgba(0,0,0,0.25)] lg:mt-0 lg:max-w-[430px] lg:px-10 lg:py-10">
        <h2 className="text-center text-[27px] font-bold text-[#45240e] lg:text-[30px]">Create Account</h2>
        <p className="mt-3 text-center text-[13px] capitalize text-[#636161]">{accountType} account</p>

        <form onSubmit={handleSubmit} className="mt-5">
          <label className={labelClass} htmlFor="full-name">Full name</label>
          <input className={inputClass} id="full-name" name="fullName" type="text" autoComplete="name" placeholder="Your full name" required />

          {accountType === "student" ? (
            <>
              <label className={labelClass} htmlFor="student-id">Student ID</label>
              <input className={inputClass} id="student-id" name="studentId" type="text" placeholder="EG/20**/****" required />
              <label className={labelClass} htmlFor="department">Department</label>
              <input className={inputClass} id="department" name="department" type="text" placeholder="Your department" required />
            </>
          ) : (
            <>
              <label className={labelClass} htmlFor="lecturer-id">Lecturer username</label>
              <input className={inputClass} id="lecturer-id" name="lecturerUsername" type="text" autoComplete="username" placeholder="Your lecturer username" required />
              <label className={labelClass} htmlFor="lecturer-id-number">Lecturer ID</label>
              <input className={inputClass} id="lecturer-id-number" name="lecturerId" type="text" placeholder="Your lecturer ID" required />
            </>
          )}

          <label className={labelClass} htmlFor="email">University email</label>
          <input className={inputClass} id="email" name="email" type="email" autoComplete="email" placeholder="name@university.edu" required />
          <label className={labelClass} htmlFor="registration-code">Registration code</label>
          <input className={inputClass} id="registration-code" name="registrationCode" type="password" autoComplete="off" placeholder="Provided by your faculty" required />
          <label className={labelClass} htmlFor="password">Password</label>
          <input className={inputClass} id="password" name="password" type="password" autoComplete="new-password" minLength={6} placeholder="At least 6 characters" required />
          <label className={labelClass} htmlFor="confirm-password">Confirm password</label>
          <input className={inputClass} id="confirm-password" name="confirmPassword" type="password" autoComplete="new-password" minLength={6} placeholder="Re-enter your password" required />

          {message && <p className="mt-4 text-center text-[13px] font-medium text-red-700" role="alert">{message}</p>}
          <button type="submit" className="mx-auto mt-6 block min-w-[160px] rounded-[9px] bg-[#45240e] px-7 py-3 text-[13px] font-medium text-white shadow-[0_3px_4px_rgba(0,0,0,0.25)] transition hover:opacity-90">Create account</button>
        </form>
        <p className="mt-5 text-center text-[12px] text-[#636161]">Already have an account? <Link href="/" className="font-semibold text-[#45240e] underline underline-offset-2">Login</Link></p>
      </section>
    </AuthLayout>
  );
}
