export type SessionFormState = {
  course: string;
  batch: string;
  location: string;
};

export type SessionControls = SessionFormState & {
  sessionId: string;
  qrToken: string;
  isActive: boolean;
};
