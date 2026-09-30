import type { SessionFormState } from "@/features/attendance/types";

export type StartAttendanceSessionRequest = SessionFormState;

/** Central endpoint map for the future Spring Boot integration. UI components should not call fetch directly. */
export const attendanceEndpoints = {
  startSession: "/api/attendance/sessions",
  endSession: (sessionId: string) => `/api/attendance/sessions/${sessionId}/end`,
  checkIn: "/api/attendance/check-in",
  records: (sessionId: string) => `/api/attendance/sessions/${sessionId}/records`,
} as const;
