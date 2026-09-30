"use client";

import { useEffect, useState } from "react";
import { QRCodeDisplay } from "@/features/attendance/components/qr-code-display";
import { fetchQrChallenge, type QrChallenge } from "@/features/attendance/services/attendance-service";

type SessionQrCardProps = { sessionId: string | null; isActive: boolean; isSaving: boolean; onEnd: () => void };

function formatCountdown(seconds: number) {
  return `00:${String(seconds).padStart(2, "0")}`;
}

export function SessionQrCard({ sessionId, isActive, isSaving, onEnd }: SessionQrCardProps) {
  const [challenge, setChallenge] = useState<QrChallenge | null>(null);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!sessionId || !isActive) {
      setChallenge(null);
      setSecondsLeft(0);
      setError(null);
      return;
    }

    let active = true;
    let refreshing = false;
    let deadline = 0;
    let retryAt = 0;

    async function refreshChallenge() {
      if (refreshing) return;
      refreshing = true;
      try {
        const next = await fetchQrChallenge(sessionId);
        if (!active) return;
        // Use the server-provided time difference; local time only advances the display countdown.
        const remainingMs = Math.max(0, Date.parse(next.expiresAt) - Date.parse(next.serverTime));
        deadline = Date.now() + remainingMs;
        setChallenge(next);
        setSecondsLeft(Math.ceil(remainingMs / 1000));
        setError(null);
        retryAt = 0;
      } catch (requestError) {
        if (!active) return;
        deadline = 0;
        setChallenge(null);
        setError(requestError instanceof Error ? requestError.message : "Could not load the current QR code.");
        retryAt = Date.now() + 5000;
      } finally {
        refreshing = false;
      }
    }

    void refreshChallenge();
    const timer = window.setInterval(() => {
      if (!deadline) {
        if (retryAt && Date.now() >= retryAt) void refreshChallenge();
        return;
      }
      const remaining = Math.max(0, deadline - Date.now());
      setSecondsLeft(Math.ceil(remaining / 1000));
      if (remaining <= 0) {
        deadline = 0;
        setChallenge(null);
        void refreshChallenge();
      }
    }, 1000);
    const onVisibilityChange = () => {
      if (document.visibilityState === "visible") void refreshChallenge();
    };
    document.addEventListener("visibilitychange", onVisibilityChange);
    return () => {
      active = false;
      window.clearInterval(timer);
      document.removeEventListener("visibilitychange", onVisibilityChange);
    };
  }, [sessionId, isActive]);

  const remaining = Math.max(0, secondsLeft);
  return <div className="qr-card"><h3>Session QR Code</h3><div className="qr-frame">{challenge ? <QRCodeDisplay token={challenge.qrToken} /> : <span className="qr-placeholder">{isActive ? "Loading secure QR code…" : "Start a session to display its QR code"}</span>}</div>{challenge ? <><strong className="session-code" aria-label={`Attendance code ${challenge.code}`}>{challenge.code}</strong><span className="qr-countdown" role="timer" aria-live="off">Refreshes in {formatCountdown(remaining)}</span><small className="session-reference">Session {sessionId}</small></> : error ? <p role="alert" className="attendance-error">{error}</p> : null}{isActive && challenge && <span className="live-label"><i /> Session active · Students can check in</span>}<button className="end-button" onClick={onEnd} disabled={!isActive || isSaving}>{isSaving ? "Saving…" : "End Session"}</button></div>;
}
