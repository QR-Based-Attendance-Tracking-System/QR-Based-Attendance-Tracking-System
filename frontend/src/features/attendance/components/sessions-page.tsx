"use client";

import { useEffect, useState } from "react";
import { SessionDetailsForm } from "@/features/attendance/components/session-details-form";
import { SessionQrCard } from "@/features/attendance/components/session-qr-card";
import type { SessionFormState } from "@/features/attendance/types";
import { endAttendanceSession, fetchActiveAttendanceSession, fetchLecturerCourses, startAttendanceSession, type LecturerCourse } from "@/features/attendance/services/attendance-service";

export function SessionsPage() {
  const [form, setForm] = useState<SessionFormState>(() => {
    const now = new Date();
    const date = [now.getFullYear(), String(now.getMonth() + 1).padStart(2, "0"), String(now.getDate()).padStart(2, "0")].join("-");
    const startTime = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
    return { course: "", location: "", date, startTime, durationHours: "1", durationMinutes: "0" };
  });
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isActive, setIsActive] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isLoadingCourses, setIsLoadingCourses] = useState(true);
  const [isRestoringSession, setIsRestoringSession] = useState(true);
  const [courses, setCourses] = useState<LecturerCourse[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    fetchLecturerCourses()
      .then((result) => {
        if (mounted) setCourses(result);
      })
      .catch((requestError: unknown) => {
        if (mounted) setError(requestError instanceof Error ? requestError.message : "Could not load your courses.");
      })
      .finally(() => {
        if (mounted) setIsLoadingCourses(false);
      });
    return () => { mounted = false; };
  }, []);

  useEffect(() => {
    let mounted = true;
    fetchActiveAttendanceSession()
      .then((session) => {
        if (!mounted || !session) return;
        setSessionId(session.id);
        setIsActive(session.active);
      })
      .catch((requestError: unknown) => {
        if (mounted) setError(requestError instanceof Error ? requestError.message : "Could not restore the active session.");
      })
      .finally(() => {
        if (mounted) setIsRestoringSession(false);
      });
    return () => { mounted = false; };
  }, []);

  function updateForm(field: keyof SessionFormState, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function startSession() {
    if (isRestoringSession) return;
    setIsSaving(true);
    setError(null);
    try {
      const session = await startAttendanceSession(form);
      setSessionId(session.id);
      setIsActive(session.active);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Could not start the attendance session.");
    } finally {
      setIsSaving(false);
    }
  }

  async function endSession() {
    if (!sessionId) return;
    setIsSaving(true);
    setError(null);
    try {
      await endAttendanceSession(sessionId);
      setIsActive(false);
      setSessionId(null);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Could not end the attendance session.");
    } finally {
      setIsSaving(false);
    }
  }

  return <div className="session-page"><h2>Start Attendance Session</h2>{error && <p role="alert" className="attendance-error">{error}</p>}{isRestoringSession && <p role="status">Checking for an active session…</p>}<div className="session-layout"><SessionDetailsForm {...form} onChange={updateForm} isActive={isActive} isSaving={isSaving || isRestoringSession} isLoadingCourses={isLoadingCourses} courses={courses} onStart={startSession} /><SessionQrCard key={isActive ? sessionId ?? "active" : "idle"} sessionId={sessionId} isActive={isActive} isSaving={isSaving} onEnd={endSession} /></div></div>;
}
