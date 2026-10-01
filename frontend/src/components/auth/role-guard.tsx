"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getCurrentUser } from "@/features/auth/services/auth-client";

export function RoleGuard({ role, children }: { role: "STUDENT" | "LECTURER"; children: React.ReactNode }) {
  const router = useRouter();
  const [allowed, setAllowed] = useState(false);
  useEffect(() => {
    let active = true;
    void getCurrentUser().then((user) => {
      if (!active) return;
      if (user.role === role) setAllowed(true);
      else router.replace("/dashboard");
    }).catch(() => router.replace("/") );
    return () => { active = false; };
  }, [role, router]);
  return allowed ? children : null;
}
