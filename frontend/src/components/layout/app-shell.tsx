"use client";

import { Header } from "@/components/layout/header";
import { Sidebar } from "@/components/layout/sidebar";

export function AppShell({ children }: Readonly<{ children: React.ReactNode }>) {
  return <main className="app-shell"><Header /><Sidebar /><section className="content">{children}</section></main>;
}
