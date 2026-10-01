import CreateAccountPage from "@/features/auth/components/create-account-page";

type Props = { searchParams: Promise<{ role?: string }> };

export default async function Page({ searchParams }: Props) {
  const params = await searchParams;
  return <CreateAccountPage role={params.role === "lecturer" ? "lecturer" : "student"} />;
}
