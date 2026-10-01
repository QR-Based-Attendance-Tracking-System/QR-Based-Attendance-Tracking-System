"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import AuthLayout from "@/components/AuthLayout";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setMessage("");

    if (!email) {
      setMessage("Please enter your university email address.");
      return;
    }

    setIsLoading(true);

    setTimeout(() => {
      setMessage(
        "If this account exists, a verification code will be sent."
      );

      setIsLoading(false);
    }, 700);
  }

  return (
    <AuthLayout>

      <section
        className="
          mt-6
          w-full
          max-w-[320px]
          rounded-[27px]
          border-2
          border-[#f8d929]
          bg-[#fcf5e0]
          px-7
          py-7
          shadow-[0_4px_5px_rgba(0,0,0,0.25)]

          lg:mt-0
          lg:max-w-[390px]
          lg:rounded-[24px]
          lg:px-9
          lg:py-9
        "
      >

        <h1
          className="
            text-center
            text-[21px]
            font-bold
            text-[#45240e]

            lg:text-[25px]
          "
        >
          Forgot Password
        </h1>

        <p
          className="
            mx-auto
            mt-4
            max-w-[250px]
            text-center
            text-[9px]
            leading-4
            text-[#636161]

            lg:max-w-[300px]
            lg:text-[10px]
          "
        >
          Enter your university email to receive a verification code.
        </p>

        <form
          onSubmit={handleSubmit}
          className="mt-6"
        >

          <label
            htmlFor="email"
            className="
              mb-1.5
              block
              text-[10px]
              text-[#636161]
            "
          >
            University Email:
          </label>

          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) =>
              setEmail(event.target.value)
            }
            placeholder="University Email"
            autoComplete="email"
            required
            className="
              h-[37px]
              w-full
              rounded-[9px]
              bg-[#d4d4d4]
              px-3
              text-[10px]
              text-[#45240e]
              outline-none
              placeholder:text-[#777]
              focus:ring-2
              focus:ring-[#45240e]

              lg:h-[42px]
              lg:text-[11px]
            "
          />

          {message && (
            <p
              className="
                mt-3
                text-center
                text-[9px]
                leading-4
                text-[#45240e]
              "
            >
              {message}
            </p>
          )}

          <button
            type="submit"
            disabled={isLoading}
            className="
              mx-auto
              mt-5
              block
              min-w-[105px]
              rounded-[8px]
              bg-[#45240e]
              px-5
              py-2
              text-[10px]
              font-medium
              text-white
              shadow-[0_2px_3px_rgba(0,0,0,0.25)]
              transition
              hover:opacity-90
              disabled:cursor-not-allowed
              disabled:opacity-60
            "
          >
            {isLoading ? "Sending..." : "Send Code"}
          </button>

        </form>

        <div className="mt-3 text-center">

          <Link
            href="/"
            className="
              text-[8px]
              text-[#45240e]
              underline
              underline-offset-2
            "
          >
            Back to Login
          </Link>

        </div>

      </section>

    </AuthLayout>
  );
}