"use client";

import { useState } from "react";
import { SessionDetailsForm } from "@/features/attendance/components/session-details-form";
import { SessionQrCard } from "@/features/attendance/components/session-qr-card";
import type { SessionFormState } from "@/features/attendance/types";

export function SessionsPage() {
  const [form, setForm] = useState<SessionFormState>({ course: "", batch: "", location: "" });
  const [isActive, setIsActive] = useState(false);
  const [sessionId, setSessionId] = useState("051 227");
  const qrToken = `${form.course}|${form.batch}|${form.location}|${sessionId}`;

  function updateForm(field: keyof SessionFormState, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function startSession() {
    setIsActive(true);
    setSessionId(`${String(Math.floor(Math.random() * 900) + 100)} ${String(Math.floor(Math.random() * 900) + 100)}`);
  }

  return <div className="session-page"><h2>Start Attendance Session</h2><div className="session-layout"><SessionDetailsForm {...form} onChange={updateForm} isActive={isActive} onStart={startSession} /><SessionQrCard qrToken={qrToken} sessionId={sessionId} isActive={isActive} onEnd={() => setIsActive(false)} /></div></div>;
}
