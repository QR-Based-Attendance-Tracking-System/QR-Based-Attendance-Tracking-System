"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { DashboardPage as LecturerDashboard } from "@/features/lecturer/components/lecturer-dashboard-page";
import StudentDashboard from "@/features/student/components/home-page";
import { AppShell } from "@/components/layout/app-shell";
import { getCurrentUser } from "@/features/auth/services/auth-client";

type Role = "STUDENT" | "LECTURER";

export default function DashboardRoute() {
  const router = useRouter();
  const [role, setRole] = useState<Role | null>(null);
  const [ready, setReady] = useState(false);
  useEffect(() => {
    let active = true;
    void getCurrentUser().then((user) => {
      if (!active) return;
      if (user.role === "STUDENT" || user.role === "LECTURER") setRole(user.role);
      else router.replace("/");
    }).catch(() => router.replace("/")).finally(() => { if (active) setReady(true); });
    return () => { active = false; };
  }, [router]);
  if (!ready || !role) return null;
  if (role === "STUDENT") return <StudentDashboard />;
  return <AppShell><LecturerDashboard /></AppShell>;
}
