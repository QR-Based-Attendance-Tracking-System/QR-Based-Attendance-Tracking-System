import { SelectField } from "@/components/forms/select-field";
import { ATTENDANCE_OPTIONS } from "@/features/attendance/constants";
import type { SessionFormState } from "@/features/attendance/types";
import type { LecturerCourse } from "@/features/attendance/services/attendance-service";
import type { FormEvent } from "react";

type SessionDetailsFormProps = SessionFormState & { onChange: (field: keyof SessionFormState, value: string) => void; isActive: boolean; isSaving: boolean; isLoadingCourses: boolean; courses: LecturerCourse[]; onStart: () => void };

export function SessionDetailsForm({ course, location, date, startTime, durationHours, durationMinutes, onChange, isActive, isSaving, isLoadingCourses, courses, onStart }: SessionDetailsFormProps) {
  const courseOptions = courses.map((item) => ({ value: item.courseCode, label: `${item.courseCode} · ${item.courseName} · ${item.batch}` }));
  const totalDurationMinutes = Number(durationHours || 0) * 60 + Number(durationMinutes || 0);
  const hasValidDuration = Number.isInteger(Number(durationHours)) && Number(durationHours) >= 0
    && Number.isInteger(Number(durationMinutes)) && Number(durationMinutes) >= 0 && Number(durationMinutes) <= 59
    && totalDurationMinutes > 0;

  function submitSession(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (hasValidDuration) onStart();
  }

  return (
    <form className="details-card" onSubmit={submitSession}>
      <h3>Session Details</h3>
      <SelectField label="Course" value={course} options={courseOptions} onChange={(value) => onChange("course", value)} />
      {!isLoadingCourses && courses.length === 0 && <p className="course-selection-hint">Add a course to your profile before starting a session.</p>}
      <label className="field">
        <span>Date</span>
        <input className="field-input" type="date" value={date} onChange={(event) => onChange("date", event.target.value)} required />
      </label>
      <label className="field">
        <span>Start Time</span>
        <input className="field-input" type="time" value={startTime} onChange={(event) => onChange("startTime", event.target.value)} required />
      </label>
      <div className="field duration-field">
        <span>Duration</span>
        <div className="duration-inputs">
          <label>
            <span>Hours</span>
            <input className="field-input" type="number" min="0" step="1" inputMode="numeric" value={durationHours} onChange={(event) => onChange("durationHours", event.target.value)} required />
            <input className="duration-slider" type="range" min="0" max="24" step="1" value={durationHours || "0"} aria-label="Adjust duration hours" onChange={(event) => onChange("durationHours", event.target.value)} />
          </label>
          <label>
            <span>Minutes</span>
            <input className="field-input" type="number" min="0" max="59" step="1" inputMode="numeric" value={durationMinutes} onChange={(event) => onChange("durationMinutes", event.target.value)} required />
            <input className="duration-slider" type="range" min="0" max="59" step="1" value={durationMinutes || "0"} aria-label="Adjust duration minutes" onChange={(event) => onChange("durationMinutes", event.target.value)} />
          </label>
        </div>
      </div>
      <SelectField label="Location" value={location} options={ATTENDANCE_OPTIONS.locations} onChange={(value) => onChange("location", value)} />
      <button className="start-button" type="submit" disabled={isActive || isSaving || isLoadingCourses || courses.length === 0 || !course || !location || !hasValidDuration}>
        {isSaving ? "Saving…" : isActive ? "Session Active" : "Start Session"}
      </button>
    </form>
  );
}
