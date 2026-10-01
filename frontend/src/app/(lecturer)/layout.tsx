import { RoleGuard } from "@/components/auth/role-guard";
import { AppShell } from "@/components/layout/app-shell";

export default function LecturerLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <RoleGuard role="LECTURER"><AppShell>{children}</AppShell></RoleGuard>;
}
