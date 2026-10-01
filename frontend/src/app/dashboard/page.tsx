"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { DashboardPage as LecturerDashboard } from "@/features/lecturer/components/lecturer-dashboard-page";
import StudentDashboard from "@/features/student/components/home-page";
import { AppShell } from "@/components/layout/app-shell";

type Role = "student" | "lecturer";

export default function DashboardRoute() {
  const router = useRouter();
  const [role] = useState<Role | null>(() => {
    if (typeof window === "undefined") return null;
    const authenticatedRole = localStorage.getItem("isLoggedIn") || sessionStorage.getItem("isLoggedIn");
    return authenticatedRole === "student" || authenticatedRole === "lecturer" ? authenticatedRole : null;
  });

  useEffect(() => {
    if (role === null) router.replace("/");
  }, [router]);

  if (!role) return null;
  if (role === "student") return <StudentDashboard />;
  return <AppShell><LecturerDashboard /></AppShell>;
}
