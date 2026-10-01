"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { Html5Qrcode } from "html5-qrcode";
import AppHeader from "@/components/AppHeader";


export default function ScanPage() {
  const router = useRouter();
  const scannerRef = useRef<Html5Qrcode | null>(null);
  const scannedRef = useRef(false);

  const [isScanning, setIsScanning] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [error, setError] = useState("");

  /* ============================================================
     STOP SCANNER
  ============================================================ */

  const stopScanner = useCallback(async () => {
    const scanner = scannerRef.current;

    if (!scanner) {
      setIsScanning(false);
      return;
    }

    try {
      await scanner.stop();
    } catch (err) {
      console.log("Scanner stop:", err);
    }

    try {
      scanner.clear();
    } catch (err) {
      console.log("Scanner clear:", err);
    }

    scannerRef.current = null;
    setIsScanning(false);
  }, []);

  /* ============================================================
     SEND ATTENDANCE TO BACKEND
  ============================================================ */

  const sendAttendance = useCallback(
    async (qrData: string) => {
      try {
        const csrfResponse = await fetch("/api/auth/csrf", { credentials: "same-origin", cache: "no-store" });
        const csrfToken = csrfResponse.headers.get("X-CSRF-TOKEN");
        if (!csrfResponse.ok || !csrfToken) throw new Error("Could not initialize secure request.");
        const response = await fetch("/api/attendance/check-in", {
          method: "POST",
          credentials: "same-origin",
          headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": csrfToken },
          body: JSON.stringify({ qrToken: qrData }),
        });

        const data = await response.json().catch(() => null);

        if (!response.ok) {
          setError(data?.message || "Attendance could not be marked.");

          setIsProcessing(false);
          return;
        }

        setSuccessMessage(
          "Attendance marked successfully."
        );

        setIsProcessing(false);
      } catch (err) {
        console.error("Backend error:", err);

        setError(
          "Could not connect to the attendance server."
        );

        setIsProcessing(false);
      }
    },
    []
  );

  /* ============================================================
     START CAMERA
  ============================================================ */

  const startScanner = useCallback(async () => {
    if (scannerRef.current) {
      return;
    }

    setError("");
    setSuccessMessage("");
    setIsProcessing(false);

    scannedRef.current = false;

    try {
      const scanner = new Html5Qrcode("qr-reader");

      scannerRef.current = scanner;

      await scanner.start(
        {
          facingMode: "environment",
        },
        {
          fps: 10,

          qrbox: {
            width: 210,
            height: 210,
          },

          aspectRatio: 1,
        },

        async (decodedText) => {
          if (scannedRef.current) {
            return;
          }

          scannedRef.current = true;

          setIsProcessing(true);

          await stopScanner();

          await sendAttendance(decodedText);
        },

        () => {
          // QR not detected.
        }
      );

      setIsScanning(true);
    } catch (err) {
      console.error("Camera error:", err);

      const scanner = scannerRef.current;
      if (scanner) {
        try {
          scanner.clear();
        } catch {
          // The scanner may not have initialized its UI yet.
        }
      }
      scannerRef.current = null;
      setIsScanning(false);

      setError(
        "Unable to access the camera. Please allow camera permission and try again."
      );
    }
  }, [sendAttendance, stopScanner]);

  /* ============================================================
     AUTO START CAMERA
  ============================================================ */

  useEffect(() => {
    const timer = setTimeout(() => {
      startScanner();
    }, 300);

    return () => {
      clearTimeout(timer);

      const scanner = scannerRef.current;

      if (scanner) {
        scanner
          .stop()
          .catch(() => {})
          .finally(() => {
            try {
              scanner.clear();
            } catch {}

            scannerRef.current = null;
          });
      }
    };
  }, [startScanner]);

  /* ============================================================
     CANCEL
  ============================================================ */

  async function handleCancel() {
    await stopScanner();

    router.replace("/dashboard");
  }

  /* ============================================================
     RETRY
  ============================================================ */

  async function handleRetry() {
    setError("");
    setSuccessMessage("");
    setIsProcessing(false);

    await startScanner();
  }

  /* ============================================================
     SUCCESS SCREEN
  ============================================================ */

  if (successMessage) {
    return (
      <main className="min-h-screen bg-[#45240e]">
        <AppHeader />

        <section
          className="
            mx-auto
            min-h-[calc(100vh-82px)]
            w-full
            max-w-[1200px]
            rounded-t-[45px]
            bg-[#fcf5e0]
            px-6
            pt-12
          "
        >
          <div className="mx-auto flex max-w-[500px] flex-col items-center pt-20 text-center">

            <div
              className="
                flex
                h-[75px]
                w-[75px]
                items-center
                justify-center
                rounded-full
                border-2
                border-[#f8d929]
                bg-white
              "
            >
              <span className="text-[42px] font-bold text-green-600">
                ✓
              </span>
            </div>

            <h1
              className="
                mt-6
                text-[26px]
                font-bold
                text-[#45240e]
              "
            >
              Attendance Marked
            </h1>

            <p
              className="
                mt-3
                max-w-[350px]
                text-[14px]
                leading-6
                text-[#636161]
              "
            >
              {successMessage}
            </p>

            <Link
              href="/dashboard"
              className="
                mt-7
                rounded-[12px]
                bg-[#45240e]
                px-8
                py-3
                text-[14px]
                font-semibold
                text-white
                shadow-[0_3px_5px_rgba(0,0,0,0.25)]
              "
            >
              Back to Dashboard
            </Link>

          </div>
        </section>
      </main>
    );
  }

  /* ============================================================
     MAIN SCAN PAGE
  ============================================================ */

  return (
    <main className="min-h-screen bg-[#45240e]">

      {/* SHARED HEADER */}

      <AppHeader />

      {/* ========================================================
          CREAM CONTENT
      ======================================================== */}

      <section
        className="
          mx-auto
          min-h-[calc(100vh-82px)]
          w-full
          max-w-[1200px]
          rounded-t-[45px]
          bg-[#45240e]
          px-5
          pt-6

          md:px-10
          md:pt-8

          lg:px-16
          lg:pt-10
        "
      >

        {/* ======================================================
            PAGE TITLE
        ====================================================== */}

        <div className="relative mx-auto max-w-[650px]">

          <Link
            href="/dashboard"
            aria-label="Back to Dashboard"
            className="
              absolute
              left-0
              top-0
              text-[25px]
              leading-none
              text-white
            "
          >
            ←
          </Link>

          <h1
            className="
              text-center
              text-[21px]
              font-semibold
              text-white

              md:text-[25px]

              lg:text-[28px]
            "
          >
            Scan QR
          </h1>

        </div>

        {/* ======================================================
            CAMERA
        ====================================================== */}

        <div
          className="
            relative
            mx-auto
            mt-8
            h-[360px]
            w-full
            max-w-[500px]
            overflow-hidden
            rounded-[10px]
            bg-black

            md:mt-10
            md:h-[430px]

            lg:h-[460px]
          "
        >

          {/* Actual camera */}

          <div
            id="qr-reader"
            className="
              absolute
              inset-0
              h-full
              w-full
            "
          />

          {/* ==================================================
              WHITE SCAN CORNERS
          ================================================== */}

          {/* Top left */}

          <div
            className="
              pointer-events-none
              absolute
              left-5
              top-5
              z-10
              h-[65px]
              w-[65px]
              border-l-[3px]
              border-t-[3px]
              border-white

              md:left-8
              md:top-8
              md:h-[75px]
              md:w-[75px]
            "
          />

          {/* Top right */}

          <div
            className="
              pointer-events-none
              absolute
              right-5
              top-5
              z-10
              h-[65px]
              w-[65px]
              border-r-[3px]
              border-t-[3px]
              border-white

              md:right-8
              md:top-8
              md:h-[75px]
              md:w-[75px]
            "
          />

          {/* Bottom left */}

          <div
            className="
              pointer-events-none
              absolute
              bottom-5
              left-5
              z-10
              h-[65px]
              w-[65px]
              border-b-[3px]
              border-l-[3px]
              border-white

              md:bottom-8
              md:left-8
              md:h-[75px]
              md:w-[75px]
            "
          />

          {/* Bottom right */}

          <div
            className="
              pointer-events-none
              absolute
              bottom-5
              right-5
              z-10
              h-[65px]
              w-[65px]
              border-b-[3px]
              border-r-[3px]
              border-white

              md:bottom-8
              md:right-8
              md:h-[75px]
              md:w-[75px]
            "
          />

          {/* ==================================================
              CAMERA LOADING
          ================================================== */}

          {!isScanning &&
            !error &&
            !isProcessing && (
              <div
                className="
                  absolute
                  inset-0
                  z-20
                  flex
                  items-center
                  justify-center
                  bg-black/30
                "
              >
                <div
                  className="
                    h-9
                    w-9
                    animate-spin
                    rounded-full
                    border-2
                    border-white/30
                    border-t-white
                  "
                />
              </div>
            )}

          {/* ==================================================
              PROCESSING
          ================================================== */}

          {isProcessing && (
            <div
              className="
                absolute
                inset-0
                z-30
                flex
                flex-col
                items-center
                justify-center
                bg-black/60
              "
            >
              <div
                className="
                  h-10
                  w-10
                  animate-spin
                  rounded-full
                  border-2
                  border-white/30
                  border-t-[#f8d929]
                "
              />

              <p className="mt-4 text-[14px] text-white">
                Checking attendance...
              </p>
            </div>
          )}

        </div>

        {/* ======================================================
            INSTRUCTION
        ====================================================== */}

        {isScanning && !isProcessing && (
          <p
            className="
              mx-auto
              mt-5
              text-center
              text-[12px]
              text-white/80

              md:text-[14px]
            "
          >
            Position the QR code inside the frame
          </p>
        )}

        {/* ======================================================
            ERROR
        ====================================================== */}

        {error && !isProcessing && (
          <div
            className="
              mx-auto
              mt-6
              max-w-[450px]
              rounded-[16px]
              bg-[#fcf5e0]
              px-6
              py-5
              text-center
            "
          >
            <p className="text-[13px] leading-5 text-red-600">
              {error}
            </p>

            <button
              onClick={handleRetry}
              className="
                mt-4
                rounded-[10px]
                bg-[#45240e]
                px-6
                py-2.5
                text-[13px]
                font-semibold
                text-white
              "
            >
              Try Again
            </button>
          </div>
        )}

        {/* ======================================================
            CANCEL
        ====================================================== */}

        {!isProcessing && (
          <div className="mt-7 flex justify-center">

            <button
              onClick={handleCancel}
              aria-label="Cancel scanning"
              className="
                flex
                h-[46px]
                w-[46px]
                items-center
                justify-center
                rounded-full
                border-2
                border-[#f8d929]
                bg-white
                shadow-[0_3px_5px_rgba(0,0,0,0.3)]
                transition
                hover:scale-105
                active:scale-95
              "
            >
              <span className="relative h-[21px] w-[21px]">

                <span
                  className="
                    absolute
                    left-1/2
                    top-1/2
                    h-[23px]
                    w-[2px]
                    -translate-x-1/2
                    -translate-y-1/2
                    rotate-45
                    bg-[#ff5b00]
                  "
                />

                <span
                  className="
                    absolute
                    left-1/2
                    top-1/2
                    h-[23px]
                    w-[2px]
                    -translate-x-1/2
                    -translate-y-1/2
                    -rotate-45
                    bg-[#ff5b00]
                  "
                />

              </span>
            </button>

          </div>
        )}

      </section>

      {/* ========================================================
          HTML5 QR CODE STYLING
      ======================================================== */}

      <style>{`
        #qr-reader {
          border: none !important;
        }

        #qr-reader__scan_region {
          width: 100% !important;
          height: 100% !important;
          border: none !important;
        }

        #qr-reader__scan_region img {
          display: none !important;
        }

        #qr-reader video {
          width: 100% !important;
          height: 100% !important;
          object-fit: cover !important;
        }

        #qr-reader__dashboard {
          display: none !important;
        }

        #qr-reader__dashboard_section {
          display: none !important;
        }

        #qr-reader__dashboard_section_csr {
          display: none !important;
        }
      `}</style>

    </main>
  );
}
