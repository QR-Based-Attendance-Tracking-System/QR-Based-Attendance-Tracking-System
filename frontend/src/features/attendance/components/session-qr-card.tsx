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
    let retryTimer = 0;
    let serverOffsetMs = 0;
    let expiryTimer = 0;

    function scheduleExpiryRefresh() {
      window.clearTimeout(expiryTimer);
      if (!deadline) return;
      const untilExpiry = deadline - Date.now();
      const untilPreRefresh = Math.max(0, untilExpiry - 1000);
      expiryTimer = window.setTimeout(() => {
        if (!active) return;
        void refreshChallenge();
        expiryTimer = window.setTimeout(() => {
          if (Date.now() >= deadline) setChallenge(null);
        }, Math.max(0, deadline - Date.now()));
      }, untilPreRefresh);
    }

    async function refreshChallenge() {
      if (refreshing) {
        refreshQueued = true;
        return;
      }
      refreshing = true;
      const version = ++requestVersion;
      const requestStartedAt = Date.now();
      try {
        const next = await fetchQrChallenge(activeSessionId);
        if (!active || version !== requestVersion) return;
        // Estimate server clock offset from the request midpoint. The backend
        // remains authoritative; this offset only drives display and refreshes.
        const expiresAt = Date.parse(next.expiresAt);
        const serverTime = Date.parse(next.serverTime);
        if (!Number.isFinite(expiresAt) || !Number.isFinite(serverTime) || expiresAt <= serverTime) {
          throw new Error("The server returned an invalid QR expiry time.");
        }
        const responseReceivedAt = Date.now();
        const roundTripMs = Math.max(0, responseReceivedAt - requestStartedAt);
        serverOffsetMs = serverTime + roundTripMs / 2 - responseReceivedAt;
        const estimatedServerNow = responseReceivedAt + serverOffsetMs;
        const remainingMs = Math.max(0, expiresAt - estimatedServerNow);
        deadline = expiresAt - serverOffsetMs;
        setChallenge(next);
        setChallengeForSessionId(activeSessionId);
        setCountdown({ sessionId: activeSessionId, seconds: Math.max(0, Math.ceil(remainingMs / 1000)) });
        setError(null);
        window.clearTimeout(retryTimer);
        scheduleExpiryRefresh();
      } catch (requestError) {
        if (!active || version !== requestVersion) return;
        if (!deadline || Date.now() >= deadline) {
          deadline = 0;
          setChallenge(null);
        }
        setError(requestError instanceof Error ? requestError.message : "Could not load the current QR code.");
        window.clearTimeout(retryTimer);
        retryTimer = window.setTimeout(() => {
          if (active) void refreshChallenge();
        }, 2000);
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
      if (!deadline) return;
      const remaining = Math.max(0, deadline - Date.now());
      setCountdown({ sessionId: activeSessionId, seconds: Math.ceil(remaining / 1000) });
      if (remaining <= 0) {
        setChallenge(null);
        if (!refreshing) void refreshChallenge();
      }
    }, 1000);
    const onVisibilityChange = () => {
      if (document.visibilityState === "visible") {
        if (!deadline || Date.now() >= deadline) setChallenge(null);
        void refreshChallenge();
      }
    };
    document.addEventListener("visibilitychange", onVisibilityChange);
    return () => {
      active = false;
      window.clearInterval(timer);
      window.clearTimeout(expiryTimer);
      window.clearTimeout(retryTimer);
      document.removeEventListener("visibilitychange", onVisibilityChange);
    };
  }, [sessionId, isActive]);

  const remaining = stateSessionId
    ? countdown?.sessionId === stateSessionId ? Math.max(0, countdown.seconds) : 0
    : 0;
  const visibleChallenge = stateMatchesSession && countdown?.seconds !== 0 ? challenge : null;
  const visibleError = stateMatchesSession ? error : null;
  return <div className="qr-card"><h3>Session QR Code</h3><div className="qr-frame">{visibleChallenge ? <QRCodeDisplay token={visibleChallenge.qrToken} /> : <span className="qr-placeholder">{isActive ? "Loading secure QR code…" : "Start a session to display its QR code"}</span>}</div>{visibleChallenge ? <><strong className="session-code" aria-label={`Attendance code ${visibleChallenge.code}`}>{visibleChallenge.code}</strong><span className="qr-countdown" role="timer" aria-live="off">Refreshes in {formatCountdown(remaining)}</span><small className="session-reference">Session {sessionId}</small></> : visibleError ? <p role="alert" className="attendance-error">{visibleError}</p> : null}{isActive && visibleChallenge && <span className="live-label"><i /> Session active · Students can check in</span>}<button className="end-button" type="button" onClick={onEnd} disabled={!isActive || isSaving}>{isSaving ? "Saving…" : "End Session"}</button></div>;
}
