import { AppShell } from "@/components/layout/app-shell";

export default function LecturerLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <AppShell>{children}</AppShell>;
}
