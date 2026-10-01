import Image from "next/image";

type AppHeaderProps = {
  showBack?: boolean;
  onBack?: () => void;
};

export default function AppHeader({
  showBack = false,
  onBack,
}: AppHeaderProps) {
  return (
    <header className="relative h-[82px] w-full bg-[#45240e]">
      {showBack && (
        <button
          type="button"
          onClick={onBack}
          aria-label="Go back"
          className="absolute left-4 top-1/2 z-10 -translate-y-1/2 text-[25px] leading-none text-white"
        >
          ←
        </button>
      )}

      <div
        className={`absolute top-[8px] flex items-center ${
          showBack ? "left-[52px]" : "left-[12px]"
        }`}
      >
        <Image
          src="/logo.png"
          alt="Attendance System Logo"
          width={48}
          height={62}
          priority
        />

        <div className="ml-2">
          <h1 className="text-[19px] font-bold leading-[21px] text-[#f8d929] md:text-[23px] md:leading-[25px]">
            Attendance System
          </h1>

          <p className="mt-[2px] text-[7px] leading-[9px] text-white md:text-[9px] md:leading-[11px]">
            Faculty Of Engineering
          </p>

          <p className="text-[7px] leading-[9px] text-white md:text-[9px] md:leading-[11px]">
            University of Ruhuna
          </p>
        </div>
      </div>
    </header>
  );
}