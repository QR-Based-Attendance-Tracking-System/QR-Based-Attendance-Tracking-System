"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { login, logout } from "@/features/auth/services/auth-client";
import Link from "next/link";
import AuthLayout from "@/components/AuthLayout";

type AccountType = "student" | "lecturer";

function UserIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
      <circle
        cx="12"
        cy="8"
        r="3.5"
        stroke="currentColor"
        strokeWidth="2"
      />
      <path
        d="M5 20c.8-3.4 3.1-5.2 7-5.2s6.2 1.8 7 5.2"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
      <rect
        x="5"
        y="10"
        width="14"
        height="10"
        rx="2"
        stroke="currentColor"
        strokeWidth="2"
      />
      <path
        d="M8 10V7a4 4 0 0 1 8 0v3"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  );
}

export default function LoginPage() {
  const router = useRouter();
  const [accountType, setAccountType] = useState<AccountType>("student");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setIsLoading(true);
    const formData = new FormData(event.currentTarget);
    const username = String(formData.get("username") || "").trim();
    const password = String(formData.get("password") || "");
    try {
      const user = await login(username, password);
      if (user.role.toLowerCase() !== accountType) {
        await logout().catch(() => undefined);
        setError(`This account is registered as a ${user.role.toLowerCase()}.`);
        setIsLoading(false);
        return;
      }
      router.replace("/dashboard");
    } catch (loginError) {
      setError(loginError instanceof Error ? loginError.message : "Login failed.");
      setIsLoading(false);
    }
  }

  return (
    <AuthLayout>

      <section
        className="
          mt-7
          w-full
          max-w-[370px]
          rounded-[30px]
          border-2
          border-[#f8d929]
          bg-[#fcf5e0]
          px-8
          py-8
          shadow-[0_5px_8px_rgba(0,0,0,0.25)]

          lg:mt-0
          lg:max-w-[430px]
          lg:px-10
          lg:py-10
        "
      >

        <h2
          className="
            text-center
            text-[27px]
            font-bold
            text-[#45240e]

            lg:text-[30px]
          "
        >
          Login
        </h2>

        <div className="mt-5 grid grid-cols-2 rounded-xl bg-[#e9dfc8] p-1" role="group" aria-label="Choose account type">
          {(["student", "lecturer"] as const).map((type) => (
            <button
              key={type}
              type="button"
              onClick={() => {
                setAccountType(type);
                setError("");
              }}
              aria-pressed={accountType === type}
              className={`rounded-lg px-3 py-2 text-sm font-semibold capitalize transition ${
                accountType === type
                  ? "bg-[#45240e] text-white shadow"
                  : "text-[#45240e] hover:bg-white/60"
              }`}
            >
              {type}
            </button>
          ))}
        </div>

        <form
          onSubmit={handleLogin}
          className="mt-8"
        >

          {/* Username */}

          <label
            htmlFor="username"
            className="mb-2 flex items-center gap-1.5 text-[13px] text-[#636161]"
          >
            <UserIcon />
            {accountType === "student" ? "Student ID:" : "Lecturer username:"}
          </label>

          <input
            id="username"
            name="username"
            type="text"
            placeholder={accountType === "student" ? "EG/20**/****" : "Lecturer username"}
            autoComplete="username"
            required
            className="
              h-[48px]
              w-full
              rounded-[11px]
              bg-[#d4d4d4]
              px-4
              text-[14px]
              text-[#45240e]
              outline-none
              placeholder:text-[#777]
              focus:ring-2
              focus:ring-[#45240e]
            "
          />

          {/* Password */}

          <label
            htmlFor="password"
            className="mb-2 mt-5 flex items-center gap-1.5 text-[13px] text-[#636161]"
          >
            <LockIcon />
            Password:
          </label>

          <input
            id="password"
            name="password"
            type="password"
            placeholder="***********"
            autoComplete="current-password"
            required
            className="
              h-[48px]
              w-full
              rounded-[11px]
              bg-[#d4d4d4]
              px-4
              text-[14px]
              text-[#45240e]
              outline-none
              placeholder:text-[#777]
              focus:ring-2
              focus:ring-[#45240e]
            "
          />

          {/* Error */}

          {error && (
            <p className="mt-4 text-center text-[13px] font-medium text-red-600">
              {error}
            </p>
          )}

          {/* Login */}

          <button
            type="submit"
            disabled={isLoading}
            className="
              mx-auto
              mt-6
              block
              min-w-[125px]
              rounded-[9px]
              bg-[#45240e]
              px-7
              py-3
              text-[13px]
              font-medium
              text-white
              shadow-[0_3px_4px_rgba(0,0,0,0.25)]
              transition
              hover:opacity-90
              disabled:cursor-not-allowed
              disabled:opacity-60
            "
          >
            {isLoading ? "Logging in..." : "Login"}
          </button>

          {/* Forgot Password */}

          <div className="mt-4 text-center">

            <Link
              href="/forgot-password"
              className="
                text-[12px]
                text-[#45240e]
                underline
                underline-offset-2
              "
            >
              Forgot Password?
            </Link>

          </div>

          <p className="mt-5 text-center text-[12px] text-[#636161]">
            New {accountType === "student" ? "student" : "lecturer"}?{" "}
            <Link
              href={`/create-account?role=${accountType}`}
              className="font-semibold text-[#45240e] underline underline-offset-2"
            >
              Create an account
            </Link>
          </p>

        </form>

      </section>

    </AuthLayout>
  );
}
