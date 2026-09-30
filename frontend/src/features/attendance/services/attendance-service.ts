import type { SessionFormState } from "@/features/attendance/types";

export type StartAttendanceSessionRequest = { courseCode: string; lecturerName: string; lectureHallName: string };

export type AttendanceSession = { id: string; active: boolean };
export type AttendanceSessionDetails = AttendanceSession & {
  courseCode: string;
  lecturerName: string;
  lectureHallId: string;
  startedAt: string;
  endedAt: string | null;
};
export type AttendanceRecord = {
  id: string;
  sessionId: string;
  studentId: string;
  checkedInAt: string;
  status: string;
};
export type QrChallenge = { qrToken: string; code: string; expiresAt: string; serverTime: string };
export type LecturerDashboardData = {
  lecturerName: string;
  totalCourses: number;
  totalSessions: number;
  totalCheckIns: number;
  activeSessions: number;
  courses: Array<{ courseCode: string; sessionCount: number; checkInCount: number; latestSessionAt: string }>;
  recentSessions: Array<{ id: string; courseCode: string; startedAt: string; active: boolean; checkInCount: number }>;
};
export type LecturerCourse = { id: string; courseCode: string; courseName: string; batch: string };
export type AddLecturerCourseRequest = { courseCode: string; courseName: string; batch: string };

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    cache: "no-store",
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message ?? body?.detail ?? `Attendance request failed (${response.status})`);
  }
  if (response.status === 204) return null as T;
  return response.json() as Promise<T>;
}

/** Central endpoint map for the Spring Boot attendance API. */
export const attendanceEndpoints = {
  startSession: "/api/attendance/sessions",
  endSession: (sessionId: string) => `/api/attendance/sessions/${sessionId}/end`,
  checkIn: "/api/attendance/check-in",
  records: (sessionId: string) => `/api/attendance/sessions/${sessionId}/records`,
  qrChallenge: (sessionId: string) => `/api/attendance/sessions/${sessionId}/qr-challenge`,
  checkInWithCode: "/api/attendance/check-in/code",
  latestSession: "/api/attendance/sessions/latest",
  lecturerDashboard: "/api/attendance/lecturer-dashboard",
  lecturerCourses: "/api/lecturers/me/courses",
} as const;

export function startAttendanceSession(form: SessionFormState) {
  const body: StartAttendanceSessionRequest = {
    courseCode: form.course,
    lecturerName: "Lecturer",
    lectureHallName: form.location,
  };
  return request<AttendanceSession>(attendanceEndpoints.startSession, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function endAttendanceSession(sessionId: string) {
  return request<AttendanceSession>(attendanceEndpoints.endSession(sessionId), { method: "POST" });
}

export function fetchQrChallenge(sessionId: string) {
  return request<QrChallenge>(attendanceEndpoints.qrChallenge(sessionId));
}

export function fetchLatestAttendanceSession() {
  return request<AttendanceSessionDetails | null>(attendanceEndpoints.latestSession);
}

export function fetchAttendanceRecords(sessionId: string) {
  return request<AttendanceRecord[]>(attendanceEndpoints.records(sessionId));
}

export function fetchLecturerDashboard() {
  return request<LecturerDashboardData>(attendanceEndpoints.lecturerDashboard);
}

export function fetchLecturerCourses() {
  return request<LecturerCourse[]>(attendanceEndpoints.lecturerCourses);
}

export function addLecturerCourse(course: AddLecturerCourseRequest) {
  return request<LecturerCourse>(attendanceEndpoints.lecturerCourses, {
    method: "POST",
    body: JSON.stringify(course),
  });
}

export function removeLecturerCourse(courseId: string) {
  return request<void>(`${attendanceEndpoints.lecturerCourses}/${courseId}`, { method: "DELETE" });
}
