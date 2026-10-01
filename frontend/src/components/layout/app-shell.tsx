"use client";

import { useEffect, useState } from "react";
import { Header } from "@/components/layout/header";
import { Sidebar } from "@/components/layout/sidebar";

export function AppShell({ children }: Readonly<{ children: React.ReactNode }>) {
  const [isNavigationOpen, setIsNavigationOpen] = useState(false);

  useEffect(() => {
    if (!isNavigationOpen) return;

    const previousOverflow = document.body.style.overflow;
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") setIsNavigationOpen(false);
    };

    document.body.style.overflow = "hidden";
    document.addEventListener("keydown", closeOnEscape);
    return () => {
      document.body.style.overflow = previousOverflow;
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, [isNavigationOpen]);

  return <main className="app-shell"><Header isNavigationOpen={isNavigationOpen} onToggleNavigation={() => setIsNavigationOpen((open) => !open)} /><Sidebar isNavigationOpen={isNavigationOpen} onNavigate={() => setIsNavigationOpen(false)} /><button className={isNavigationOpen ? "navigation-backdrop is-visible" : "navigation-backdrop"} type="button" aria-label="Close navigation" aria-hidden={!isNavigationOpen} disabled={!isNavigationOpen} tabIndex={isNavigationOpen ? 0 : -1} onClick={() => setIsNavigationOpen(false)} /><section className="content">{children}</section></main>;
}
