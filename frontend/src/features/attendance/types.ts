export type SessionFormState = {
  course: string;
  location: string;
};

export type SessionControls = SessionFormState & {
  sessionId: string;
  qrToken: string;
  isActive: boolean;
};
