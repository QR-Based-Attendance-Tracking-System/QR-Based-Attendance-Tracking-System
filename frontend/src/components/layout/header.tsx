import { Crest } from "@/components/branding/crest";
import { Icon } from "@/components/ui/icon";

export function Header() {
  return <header className="topbar"><button className="menu-button" aria-label="Open menu"><Icon name="menu" /></button><div className="brand"><Crest /><div><h1>Attendance System</h1><p>Faculty Of Engineering<br />University of Ruhuna</p></div></div><div className="topbar-right"><button className="icon-button" aria-label="Notifications"><Icon name="bell" /></button><div className="user-avatar">PB</div><div className="user-copy"><strong>Dr. Praveen Bandara</strong><span>Lecturer</span></div></div></header>;
}
