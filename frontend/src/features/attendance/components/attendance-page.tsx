"use client";

import { useEffect, useState } from "react";
import {
  fetchAttendanceRecords,
  fetchLatestAttendanceSession,
  type AttendanceRecord,
  type AttendanceSessionDetails,
} from "@/features/attendance/services/attendance-service";

function formatDateTime(value: string) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}

export function AttendancePage() {
  const [session, setSession] = useState<AttendanceSessionDetails | null>(null);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    let loadingSession = false;

    async function refresh() {
      if (loadingSession) return;
      loadingSession = true;
      try {
        const latest = await fetchLatestAttendanceSession();
        if (!mounted) return;
        if (!latest) {
          setSession(null);
          setRecords([]);
          setError(null);
          return;
        }
        const latestRecords = await fetchAttendanceRecords(latest.id);
        if (!mounted) return;
        setSession(latest);
        setRecords(latestRecords);
        setError(null);
      } catch (requestError) {
        if (mounted) setError(requestError instanceof Error ? requestError.message : "Could not load attendance records.");
      } finally {
        loadingSession = false;
        if (mounted) setLoading(false);
      }
    }

    void refresh();
    const timer = window.setInterval(() => void refresh(), 5000);
    const refreshWhenVisible = () => {
      if (document.visibilityState === "visible") void refresh();
    };
    document.addEventListener("visibilitychange", refreshWhenVisible);
    return () => {
      mounted = false;
      window.clearInterval(timer);
      document.removeEventListener("visibilitychange", refreshWhenVisible);
    };
  }, []);

  return (
    <div className="attendance-page">
      <div className="attendance-page-heading">
        <div>
          <h2>Attendance Records</h2>
          <p>Students marked present in the latest attendance session.</p>
        </div>
        <span className="attendance-refresh-label"><i /> Updates automatically</span>
      </div>

      {error && <p className="attendance-error" role="alert">{error}</p>}
      {loading ? (
        <div className="attendance-empty" role="status">Loading attendance records…</div>
      ) : !session ? (
        <div className="attendance-empty">No attendance session has been started yet.</div>
      ) : (
        <>
          <section className="attendance-summary" aria-label="Session summary">
            <div><span>Course</span><strong>{session.courseCode}</strong></div>
            <div><span>Started</span><strong>{formatDateTime(session.startedAt)}</strong></div>
            <div><span>Session status</span><strong className={session.active ? "attendance-status active" : "attendance-status ended"}>{session.active ? "Active" : "Ended"}</strong></div>
            <div className="attendance-total"><span>Students present</span><strong>{records.length}</strong></div>
          </section>

          <section className="attendance-records panel" aria-labelledby="attendance-list-heading">
            <div className="panel-heading">
              <div><h3 id="attendance-list-heading">Student check-ins</h3><span className="muted">Latest session · {records.length} {records.length === 1 ? "student" : "students"}</span></div>
            </div>
            {records.length === 0 ? (
              <div className="attendance-empty compact">No students have checked in for this session yet.</div>
            ) : (
              <div className="attendance-table-wrap">
                <table className="attendance-table">
                  <thead><tr><th scope="col">#</th><th scope="col">Student ID</th><th scope="col">Marked at</th><th scope="col">Status</th></tr></thead>
                  <tbody>{records.map((record, index) => (
                    <tr key={record.id}>
                      <td>{index + 1}</td>
                      <td className="attendance-student-id">{record.studentId}</td>
                      <td>{formatDateTime(record.checkedInAt)}</td>
                      <td><span className="attendance-record-status">{record.status}</span></td>
                    </tr>
                  ))}</tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}
