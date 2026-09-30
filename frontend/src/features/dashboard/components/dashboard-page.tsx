"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Icon } from "@/components/ui/icon";
import { fetchLecturerDashboard, type LecturerDashboardData } from "@/features/attendance/services/attendance-service";
import { MyCoursesPanel } from "@/features/dashboard/components/my-courses-panel";

function formatDateTime(value: string) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric", hour: "numeric", minute: "2-digit" }).format(date);
}

export function DashboardPage() {
  const [dashboard, setDashboard] = useState<LecturerDashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    let refreshing = false;
    async function refresh() {
      if (refreshing) return;
      refreshing = true;
      try {
        const data = await fetchLecturerDashboard();
        if (mounted) {
          setDashboard(data);
          setError(null);
        }
      } catch (requestError) {
        if (mounted) setError(requestError instanceof Error ? requestError.message : "Could not load lecturer dashboard.");
      } finally {
        refreshing = false;
        if (mounted) setLoading(false);
      }
    }

    void refresh();
    const timer = window.setInterval(() => void refresh(), 15000);
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

  const lecturerName = dashboard?.lecturerName?.trim() || "Lecturer";
  return (
    <div className="dashboard-page">
      <div className="welcome-row">
        <div>
          <h2>Welcome, <b>{lecturerName}</b></h2>
          <p>Your courses, sessions, and attendance at a glance.</p>
        </div>
        <div className="date-card"><Icon name="calendar" /><div><strong>Lecturer overview</strong><span>Courses and attendance</span></div></div>
      </div>

      <div className="dashboard-actions">
        <div><strong>Ready to take attendance?</strong><span>Start a session and display its rotating QR code.</span></div>
        <Link className="dashboard-create-button" href="/sessions"><span aria-hidden="true">＋</span> Create QR session</Link>
      </div>

      {error && <p className="attendance-error" role="alert">{error}</p>}
      {loading ? (
        <div className="attendance-empty" role="status">Loading your lecturer overview…</div>
      ) : (
        <>
          <div className="dashboard-data-grid">
              <MyCoursesPanel />
              <section className="panel dashboard-recent-panel" aria-labelledby="dashboard-recent-heading">
                <div className="panel-heading"><div><h3 id="dashboard-recent-heading">Recent sessions</h3><span className="muted">Your latest five sessions</span></div><Link href="/sessions">New session <span aria-hidden="true">→</span></Link></div>
                {dashboard?.recentSessions.length ? <div className="dashboard-recent-list">
                  {dashboard.recentSessions.map((session) => (
                    <article className="dashboard-recent-row" key={session.id}>
                      <i className={session.active ? "recent-session-dot active" : "recent-session-dot"} />
                      <div><strong>{session.courseCode}</strong><span>{formatDateTime(session.startedAt)}</span></div>
                      <b>{session.checkInCount}</b>
                    </article>
                  ))}
                </div> : <div className="my-courses-empty">No sessions recorded yet.</div>}
              </section>
          </div>
        </>
      )}
    </div>
  );
}
