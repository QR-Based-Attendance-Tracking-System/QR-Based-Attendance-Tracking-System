"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { Icon } from "@/components/ui/icon";
import { getCurrentUser, logout, type AuthUser } from "@/features/auth/services/auth-client";

type HeaderProps = { isNavigationOpen: boolean; onToggleNavigation: () => void };

export function Header({ isNavigationOpen, onToggleNavigation }: HeaderProps) {
  const router = useRouter();
  const [user, setUser] = useState<AuthUser | null>(null);
  useEffect(() => { void getCurrentUser().then(setUser).catch(() => router.replace("/")); }, [router]);
  async function handleLogout() {
    try { await logout(); } finally { router.replace("/"); }
  }
  const name = user?.fullName || user?.username || "";
  const initials = name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join("");
  return (
    <header className="topbar">
      <button className="menu-button" type="button" aria-label={isNavigationOpen ? "Close navigation" : "Open navigation"} aria-expanded={isNavigationOpen} aria-controls="primary-navigation" onClick={onToggleNavigation}><Icon name="menu" /></button>
      <div className="brand"><Image src="/logo.png" alt="University of Ruhuna logo" width={48} height={62} priority /><div><h1>Attendance System</h1><p>Faculty Of Engineering<br />University of Ruhuna</p></div></div>
      <div className="topbar-right">
        <div className="user-avatar" aria-label="Account profile">{initials || "?"}</div>
        <div className="user-copy"><strong>{name || "Account"}</strong><span>{user?.role === "LECTURER" ? "Lecturer" : "Student"}</span></div>
        <button type="button" onClick={handleLogout} className="auth-logout-button">Log out</button>
      </div>
    </header>
  );
}
