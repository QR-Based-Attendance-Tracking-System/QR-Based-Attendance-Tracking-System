"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Icon } from "@/components/ui/icon";

const navigation = [{ href: "/dashboard", label: "Dashboard", icon: "home" as const }, { href: "/sessions", label: "Sessions", icon: "calendar" as const }, { href: "/attendance", label: "Attendance", icon: "users" as const }, { href: "#", label: "Profile", icon: "profile" as const }];

type SidebarProps = { isNavigationOpen: boolean; onNavigate: () => void };

export function Sidebar({ isNavigationOpen, onNavigate }: SidebarProps) {
  const pathname = usePathname();
  return <aside className={isNavigationOpen ? "sidebar is-open" : "sidebar"}><nav id="primary-navigation" aria-label="Main navigation">{navigation.map((item) => <Link key={item.label} onClick={onNavigate} className={pathname === item.href ? "nav-item active" : "nav-item"} href={item.href}><Icon name={item.icon} /><span>{item.label}</span></Link>)}</nav></aside>;
}
