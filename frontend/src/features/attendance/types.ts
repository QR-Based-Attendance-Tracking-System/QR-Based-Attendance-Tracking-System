export type SessionFormState = {
  course: string;
  location: string;
  date: string;
  startTime: string;
  durationHours: string;
  durationMinutes: string;
};

export type SessionControls = SessionFormState & {
  sessionId: string;
  qrToken: string;
  isActive: boolean;
};
