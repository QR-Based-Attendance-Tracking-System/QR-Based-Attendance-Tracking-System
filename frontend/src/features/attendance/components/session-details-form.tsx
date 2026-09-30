import { SelectField } from "@/components/forms/select-field";
import { ATTENDANCE_OPTIONS } from "@/features/attendance/constants";
import type { SessionFormState } from "@/features/attendance/types";
import type { LecturerCourse } from "@/features/attendance/services/attendance-service";

type SessionDetailsFormProps = SessionFormState & { onChange: (field: keyof SessionFormState, value: string) => void; isActive: boolean; isSaving: boolean; isLoadingCourses: boolean; courses: LecturerCourse[]; onStart: () => void };

export function SessionDetailsForm({ course, location, onChange, isActive, isSaving, isLoadingCourses, courses, onStart }: SessionDetailsFormProps) {
  const courseOptions = courses.map((item) => ({ value: item.courseCode, label: `${item.courseCode} · ${item.courseName} · ${item.batch}` }));
  return <div className="details-card"><h3>Session Details</h3><SelectField label="Course" value={course} options={courseOptions} onChange={(value) => onChange("course", value)} />{!isLoadingCourses && courses.length === 0 && <p className="course-selection-hint">Add a course to your profile before starting a session.</p>}<SelectField label="Date" value={ATTENDANCE_OPTIONS.dates[0]} options={ATTENDANCE_OPTIONS.dates} onChange={() => undefined} /><SelectField label="Start Time" value={ATTENDANCE_OPTIONS.startTimes[0]} options={ATTENDANCE_OPTIONS.startTimes} onChange={() => undefined} /><SelectField label="Duration" value={ATTENDANCE_OPTIONS.durations[0]} options={ATTENDANCE_OPTIONS.durations} onChange={() => undefined} /><SelectField label="Location" value={location} options={ATTENDANCE_OPTIONS.locations} onChange={(value) => onChange("location", value)} /><button className="start-button" onClick={onStart} disabled={isActive || isSaving || isLoadingCourses || courses.length === 0 || !course || !location}>{isSaving ? "Saving…" : isActive ? "Session Active" : "Start Session"}</button></div>;
}
