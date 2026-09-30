import { Icon } from "@/components/ui/icon";

export function DashboardPage() {
  return <div className="dashboard-page"><div className="welcome-row"><div><h2>Good Morning, <b>Dr. Praveen!</b></h2><p>Here’s an Overview of your classes and attendance</p></div><div className="date-card"><Icon name="calendar" /><div><strong>Monday</strong><span>28 September 2026</span></div></div></div></div>;
}
