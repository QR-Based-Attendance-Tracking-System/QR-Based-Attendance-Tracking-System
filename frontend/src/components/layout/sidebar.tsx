"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Icon } from "@/components/ui/icon";

const navigation = [{ href: "/dashboard", label: "Dashboard", icon: "home" as const }, { href: "/sessions", label: "Sessions", icon: "calendar" as const }, { href: "#", label: "Attendance", icon: "users" as const }, { href: "#", label: "Profile", icon: "profile" as const }];

export function Sidebar() {
  const pathname = usePathname();
  return <aside className="sidebar"><nav>{navigation.map((item) => <Link key={item.label} className={pathname === item.href ? "nav-item active" : "nav-item"} href={item.href}><Icon name={item.icon} /><span>{item.label}</span></Link>)}</nav></aside>;
}
