"use client";

import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import {
  addLecturerCourse,
  fetchLecturerCourses,
  removeLecturerCourse,
  type LecturerCourse,
} from "@/features/attendance/services/attendance-service";

export function MyCoursesPanel() {
  const [courses, setCourses] = useState<LecturerCourse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [courseCode, setCourseCode] = useState("");
  const [courseName, setCourseName] = useState("");
  const [saving, setSaving] = useState(false);
  const [removingId, setRemovingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    fetchLecturerCourses()
      .then((result) => {
        if (mounted) setCourses(result);
      })
      .catch((requestError: unknown) => {
        if (mounted) setError(requestError instanceof Error ? requestError.message : "Could not load your courses.");
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });
    return () => { mounted = false; };
  }, []);

  async function submitCourse(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const course = await addLecturerCourse({ courseCode: courseCode.trim(), courseName: courseName.trim() });
      setCourses((current) => current.some((item) => item.id === course.id)
        ? current
        : [...current, course].sort((a, b) => a.courseCode.localeCompare(b.courseCode)));
      setCourseCode("");
      setCourseName("");
      setShowForm(false);
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Could not add the course.");
    } finally {
      setSaving(false);
    }
  }

  async function removeCourse(course: LecturerCourse) {
    setRemovingId(course.id);
    setError(null);
    try {
      await removeLecturerCourse(course.id);
      setCourses((current) => current.filter((item) => item.id !== course.id));
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : "Could not remove the course.");
    } finally {
      setRemovingId(null);
    }
  }

  return (
    <section className="panel dashboard-course-panel my-courses-panel" aria-labelledby="my-courses-heading">
      <div className="panel-heading">
        <div><h3 id="my-courses-heading">My Courses</h3><span className="muted">Courses linked to your lecturer profile</span></div>
        <button type="button" className="add-course-toggle" onClick={() => { setShowForm((shown) => !shown); setError(null); }} aria-expanded={showForm}>
          {showForm ? "Cancel" : "+ Add Course"}
        </button>
      </div>

      {error && <p className="attendance-error course-panel-error" role="alert">{error}</p>}
      {showForm && (
        <form className="course-add-form" onSubmit={submitCourse}>
          <label><span>Course code</span><input value={courseCode} onChange={(event) => setCourseCode(event.target.value)} maxLength={50} required placeholder="e.g. CS2040" /></label>
          <label><span>Course name</span><input value={courseName} onChange={(event) => setCourseName(event.target.value)} maxLength={150} required placeholder="e.g. Data Structures" /></label>
          <button type="submit" className="course-save-button" disabled={saving}>{saving ? "Adding…" : "Add course"}</button>
        </form>
      )}

      {loading ? (
        <div className="my-courses-empty" role="status">Loading courses…</div>
      ) : courses.length === 0 ? (
        <div className="my-courses-empty">You haven’t added any courses yet.</div>
      ) : (
        <div className="dashboard-course-list">
          {courses.map((course) => (
            <article className="dashboard-course-row" key={course.id}>
              <div className="course-avatar"><span aria-hidden="true">{course.courseCode.slice(0, 2)}</span></div>
              <div className="dashboard-course-name"><strong>{course.courseName}</strong><span>{course.courseCode}</span></div>
              <button type="button" className="course-remove-button" onClick={() => void removeCourse(course)} disabled={removingId === course.id} aria-label={`Remove ${course.courseName} from your courses`}>
                {removingId === course.id ? "Removing…" : "Remove"}
              </button>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
