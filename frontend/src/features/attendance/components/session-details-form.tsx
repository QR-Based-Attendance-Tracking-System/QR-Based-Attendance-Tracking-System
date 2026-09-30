import { SelectField } from "@/components/forms/select-field";
import { ATTENDANCE_OPTIONS } from "@/features/attendance/constants";
import type { SessionFormState } from "@/features/attendance/types";

type SessionDetailsFormProps = SessionFormState & { onChange: (field: keyof SessionFormState, value: string) => void; isActive: boolean; onStart: () => void };

export function SessionDetailsForm({ course, batch, location, onChange, isActive, onStart }: SessionDetailsFormProps) {
  return <div className="details-card"><h3>Session Details</h3><SelectField label="Course" value={course} options={ATTENDANCE_OPTIONS.courses} onChange={(value) => onChange("course", value)} /><SelectField label="Batch" value={batch} options={ATTENDANCE_OPTIONS.batches} onChange={(value) => onChange("batch", value)} /><SelectField label="Date" value={ATTENDANCE_OPTIONS.dates[0]} options={ATTENDANCE_OPTIONS.dates} onChange={() => undefined} /><SelectField label="Start Time" value={ATTENDANCE_OPTIONS.startTimes[0]} options={ATTENDANCE_OPTIONS.startTimes} onChange={() => undefined} /><SelectField label="Duration" value={ATTENDANCE_OPTIONS.durations[0]} options={ATTENDANCE_OPTIONS.durations} onChange={() => undefined} /><SelectField label="Location" value={location} options={ATTENDANCE_OPTIONS.locations} onChange={(value) => onChange("location", value)} /><button className="start-button" onClick={onStart} disabled={isActive}>{isActive ? "Session Active" : "Start Session"}</button></div>;
}
