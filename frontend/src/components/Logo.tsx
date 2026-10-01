import Image from "next/image";

export default function Logo() {
  return (
    <Image
      src="/logo.png"
      alt="Attendance System Logo"
      width={207}
      height={207}
      priority
      className="h-full w-full object-contain"
    />
  );
}