"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import AppHeader from "@/components/AppHeader";
{/* From  profile*/}
const student = {
  name: "Gayan", 
  registrationNumber: "EG/2023/5501",
  department: "Computer Engineering",
};

export default function HomePage() {
  const router = useRouter();

  useEffect(() => {
    const loggedIn =
      localStorage.getItem("isLoggedIn") ||
      sessionStorage.getItem("isLoggedIn");

    if (!loggedIn) {
      router.replace("/");
    }
  }, [router]);

  return (
    <main className="min-h-screen bg-[#45240e]">

      {/* ======================================================
          SHARED HEADER
      ======================================================= */}

      <AppHeader />

      {/* ======================================================
          CREAM MAIN PANEL
      ======================================================= */}

      <section
        className="
          mx-auto
          min-h-[calc(100vh-115px)]
          w-full
          rounded-t-[35px]
          bg-[#fcf5e0]
          px-5
          pb-[100px]
          pt-8

          md:min-h-[calc(100vh-140px)]
          md:rounded-t-[45px]
          md:px-10
          md:pb-[110px]
          md:pt-10

          lg:min-h-[calc(100vh-157px)]
          lg:max-w-[1200px]
          lg:rounded-t-[55px]
          lg:px-16
          lg:pb-[120px]
          lg:pt-12
        "
      >

        {/* ==================================================
            CONTENT CONTAINER
        =================================================== */}

        <div className="mx-auto w-full">

          {/* =================================================
              STUDENT HEADER
          ================================================= */}

          <section
            className="
              flex
              w-full
              items-start
              justify-between
            "
          >

            {/* Student details */}

            <div>
              <h2
                className="
                  text-[26px]
                  font-bold
                  leading-[28px]
                  text-[#45240e]

                  md:text-[35px]
                  md:leading-9

                  lg:text-[40px]
                  lg:leading-[42px]
                "
              >
                Good Morning,
                <br />
                {student.name}!
              </h2>

              <p
                className="
                  mt-2
                  text-[8px]
                  text-[#636161]

                  md:text-[11px]

                  lg:text-[13px]
                "
              >
                {student.registrationNumber} |{" "}
                {student.department}
              </p>
            </div>

            {/* Profile */}

            <Link
              href="/profile"
              aria-label="Profile"
              className="
                flex
                h-[50px]
                w-[50px]
                shrink-0
                items-center
                justify-center
                rounded-full
                border
                border-[#45240e]
                bg-[#d4d4d4]

                md:h-[64px]
                md:w-[64px]

                lg:h-[72px]
                lg:w-[72px]
              "
            >
              <span
                className="
                  text-[21px]

                  md:text-[27px]

                  lg:text-[31px]
                "
              >
                👤
              </span>
            </Link>

          </section>

          {/* =================================================
              MARK ATTENDANCE
          ================================================= */}

          <section
            className="
              flex
              justify-center
              pt-[65px]

              md:pt-[85px]

              lg:pt-[95px]
            "
          >

            <div
              className="
                w-[260px]
                rounded-[28px]
                border-2
                border-[#f8d929]
                bg-[#ffeb8a]
                px-5
                py-5
                text-center
                shadow-[0_3px_6px_rgba(0,0,0,0.08)]

                md:w-[330px]
                md:rounded-[32px]
                md:px-7
                md:py-7

                lg:w-[380px]
                lg:rounded-[36px]
                lg:px-8
                lg:py-8
              "
            >

              {/* Title */}

              <h3
                className="
                  text-[22px]
                  font-bold
                  leading-6
                  text-[#45240e]

                  md:text-[26px]
                  md:leading-7

                  lg:text-[30px]
                  lg:leading-8
                "
              >
                Mark Attendance
              </h3>

              {/* =================================================
                  QR CODE
              ================================================= */}

              <div
                className="
                  mx-auto
                  mt-5
                  flex
                  h-[120px]
                  w-[120px]
                  items-center
                  justify-center

                  md:mt-6
                  md:h-[145px]
                  md:w-[145px]

                  lg:mt-7
                  lg:h-[170px]
                  lg:w-[170px]
                "
              >
                <Image
                  src="/attendance-qr.png"
                  alt="Attendance QR Code"
                  width={170}
                  height={170}
                  priority
                  className="
                    h-[120px]
                    w-[120px]
                    object-contain

                    md:h-[145px]
                    md:w-[145px]

                    lg:h-[170px]
                    lg:w-[170px]
                  "
                />
              </div>

              {/* Description */}

              <p
                className="
                  mx-auto
                  mt-4
                  max-w-[200px]
                  text-[10px]
                  leading-[13px]
                  text-[#636161]

                  md:max-w-[240px]
                  md:text-[12px]
                  md:leading-[15px]

                  lg:max-w-[270px]
                  lg:text-[13px]
                  lg:leading-[17px]
                "
              >
                Scan the qr code provided
                <br />
                by your lecturer to mark
                <br />
                your attendance
              </p>

              {/* =================================================
                  SCAN BUTTON
              ================================================= */}

              <Link
                href="/scan"
                className="
                  mx-auto
                  mt-5
                  flex
                  h-[38px]
                  w-[145px]
                  items-center
                  justify-center
                  gap-1
                  rounded-[10px]
                  bg-[#45240e]
                  text-[13px]
                  font-semibold
                  text-white
                  shadow-[0_3px_4px_rgba(0,0,0,0.3)]
                  transition
                  hover:opacity-90

                  md:h-[42px]
                  md:w-[165px]
                  md:text-[14px]

                  lg:h-[46px]
                  lg:w-[180px]
                  lg:text-[15px]
                "
              >
                <span>⛶</span>
                Scan QR
              </Link>

            </div>

          </section>

        </div>

      </section>

      {/* ======================================================
          BOTTOM NAVIGATION
      ======================================================= */}

      <nav
        className="
          fixed
          bottom-0
          left-0
          right-0
          z-50
          h-[45px]
          bg-[#fcf5e0]
          shadow-[0_-4px_12px_rgba(0,0,0,0.18)]

          md:h-[58px]

          lg:h-[65px]
        "
      >

        <div
          className="
            mx-auto
            grid
            h-full
            max-w-[700px]
            grid-cols-3
          "
        >

          {/* Home */}

          <Link
            href="/home"
            className="
              flex
              flex-col
              items-center
              justify-center
              text-[#45240e]
            "
          >
            <span
              className="
                text-[17px]

                md:text-[21px]

                lg:text-[23px]
              "
            >
              ⌂
            </span>

            <span
              className="
                text-[8px]
                font-medium

                md:text-[10px]

                lg:text-[11px]
              "
            >
              Home
            </span>
          </Link>

          {/* Attendance */}

          <Link
            href="/attendance"
            className="
              flex
              flex-col
              items-center
              justify-center
              text-[#636161]
            "
          >
            <span
              className="
                text-[16px]

                md:text-[20px]

                lg:text-[22px]
              "
            >
              ▣
            </span>

            <span
              className="
                text-[8px]
                font-medium

                md:text-[10px]

                lg:text-[11px]
              "
            >
              Attendance
            </span>
          </Link>

          {/* Profile */}

          <Link
            href="/profile"
            className="
              flex
              flex-col
              items-center
              justify-center
              text-[#636161]
            "
          >
            <span
              className="
                text-[16px]

                md:text-[20px]

                lg:text-[22px]
              "
            >
              ◎
            </span>

            <span
              className="
                text-[8px]
                font-medium

                md:text-[10px]

                lg:text-[11px]
              "
            >
              Profile
            </span>
          </Link>

        </div>

      </nav>

    </main>
  );
}