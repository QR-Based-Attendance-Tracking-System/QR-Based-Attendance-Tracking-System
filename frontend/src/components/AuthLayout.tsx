import Image from "next/image";
import React from "react";

type AuthLayoutProps = {
  children: React.ReactNode;
};

export default function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <main className="min-h-screen bg-[#45240e]">
      <div className="mx-auto flex min-h-screen w-full max-w-[1440px]">

        {/* Desktop Branding */}
        <section className="hidden w-1/2 items-center justify-center lg:flex">
          <div className="flex flex-col items-center text-center">

            <Image
              src="/logo.png"
              alt="University of Ruhuna Logo"
              width={240}
              height={240}
              priority
              className="h-[240px] w-[240px] object-contain"
            />

            <h1 className="mt-2 text-[30px] font-bold text-[#f8d929]">
              Attendance System
            </h1>

            <p className="mt-3 text-[14px] text-white">
              Faculty Of Engineering
            </p>

            <p className="text-[14px] text-white">
              University of Ruhuna
            </p>

          </div>
        </section>

        {/* Form */}
        <section className="flex w-full items-center justify-center px-5 py-8 lg:w-1/2 lg:px-12">

          {/* Mobile */}
          <div className="flex w-full flex-col items-center lg:hidden">

            <Image
              src="/logo.png"
              alt="University of Ruhuna Logo"
              width={175}
              height={175}
              priority
              className="h-[175px] w-[175px] object-contain"
            />

            <h1 className="mt-[-2px] text-center text-[27px] font-bold text-[#f8d929]">
              Attendance System
            </h1>

            {children}

            <div className="mt-8 text-center text-[13px] leading-5 text-white">
              <p>Faculty Of Engineering</p>
              <p>University of Ruhuna</p>
            </div>

          </div>

          {/* Desktop */}
          <div className="hidden w-full max-w-[430px] lg:block">
            {children}
          </div>

        </section>

      </div>
    </main>
  );
}