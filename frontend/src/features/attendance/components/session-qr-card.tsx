"use client";

import { useEffect, useState } from "react";
import { QRCodeDisplay } from "@/features/attendance/components/qr-code-display";
import { fetchQrChallenge, type QrChallenge } from "@/features/attendance/services/attendance-service";

type SessionQrCardProps = { sessionId: string | null; isActive: boolean; isSaving: boolean; onEnd: () => void };

function formatCountdown(seconds: number) {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(remainingSeconds).padStart(2, "0")}`;
}

export function SessionQrCard({ sessionId, isActive, isSaving, onEnd }: SessionQrCardProps) {
  const [challenge, setChallenge] = useState<QrChallenge | null>(null);
  const [challengeForSessionId, setChallengeForSessionId] = useState<string | null>(null);
  const [countdown, setCountdown] = useState<{ sessionId: string; seconds: number } | null>(null);
  const [error, setError] = useState<string | null>(null);
  const stateSessionId = isActive ? sessionId : null;
  const stateMatchesSession = stateSessionId === challengeForSessionId;

  useEffect(() => {
    if (!sessionId || !isActive) {
      return;
    }

    const activeSessionId = sessionId;

    let active = true;
    let refreshing = false;
    let refreshQueued = false;
    let requestVersion = 0;
    let deadline = 0;
    let retryAt = 0;

    deadline = Date.now() + 30_000;
    const initialTick = window.setTimeout(() => {
      if (active) setCountdown({ sessionId: activeSessionId, seconds: 30 });
    }, 0);

    async function refreshChallenge() {
      if (refreshing) {
        refreshQueued = true;
        return;
      }
      refreshing = true;
      const version = ++requestVersion;
      try {
        const next = await fetchQrChallenge(activeSessionId);
        if (!active || version !== requestVersion) return;
        // Use the server-provided time difference; local time only advances the display countdown.
        const remainingMs = Math.max(0, Date.parse(next.expiresAt) - Date.parse(next.serverTime));
        deadline = Date.now() + remainingMs;
        setChallenge(next);
        setChallengeForSessionId(activeSessionId);
        setCountdown({ sessionId: activeSessionId, seconds: Math.ceil(remainingMs / 1000) });
        setError(null);
        retryAt = 0;
      } catch (requestError) {
        if (!active || version !== requestVersion) return;
        deadline = 0;
        setChallenge(null);
        setError(requestError instanceof Error ? requestError.message : "Could not load the current QR code.");
        retryAt = Date.now() + 5000;
      } finally {
        if (version === requestVersion) refreshing = false;
        if (active && refreshQueued) {
          refreshQueued = false;
          void refreshChallenge();
        }
      }
    }

    void refreshChallenge();
    const timer = window.setInterval(() => {
      if (!deadline) {
        if (retryAt && Date.now() >= retryAt) void refreshChallenge();
        return;
      }
      const remaining = Math.max(0, deadline - Date.now());
      setCountdown({ sessionId: activeSessionId, seconds: Math.ceil(remaining / 1000) });
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
      window.clearTimeout(initialTick);
      window.clearInterval(timer);
      document.removeEventListener("visibilitychange", onVisibilityChange);
    };
  }, [sessionId, isActive]);

  const remaining = stateSessionId
    ? countdown?.sessionId === stateSessionId ? Math.max(0, countdown.seconds) : 30
    : 0;
  const visibleChallenge = stateMatchesSession ? challenge : null;
  const visibleError = stateMatchesSession ? error : null;
  return <div className="qr-card"><h3>Session QR Code</h3><div className="qr-frame">{visibleChallenge ? <QRCodeDisplay token={visibleChallenge.qrToken} /> : <span className="qr-placeholder">{isActive ? "Loading secure QR code…" : "Start a session to display its QR code"}</span>}</div>{visibleChallenge ? <><strong className="session-code" aria-label={`Attendance code ${visibleChallenge.code}`}>{visibleChallenge.code}</strong><span className="qr-countdown" role="timer" aria-live="off">Refreshes in {formatCountdown(remaining)}</span><small className="session-reference">Session {sessionId}</small></> : visibleError ? <p role="alert" className="attendance-error">{visibleError}</p> : null}{isActive && visibleChallenge && <span className="live-label"><i /> Session active · Students can check in</span>}<button className="end-button" type="button" onClick={onEnd} disabled={!isActive || isSaving}>{isSaving ? "Saving…" : "End Session"}</button></div>;
}
