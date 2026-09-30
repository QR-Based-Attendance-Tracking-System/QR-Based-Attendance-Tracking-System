"use client";

import { useMemo } from "react";

type QrCodeProps = { value: string };

/** Visual QR placeholder. Replace its internals with the backend QR renderer when the API is connected. */
export function QrCode({ value }: QrCodeProps) {
  const size = 29;
  const modules = useMemo(() => {
    const hash = Array.from(value).reduce((total, char) => total + char.charCodeAt(0), 0);
    return Array.from({ length: size }, (_, row) => Array.from({ length: size }, (_, column) => {
      const finder = (startRow: number, startColumn: number) => row >= startRow && row < startRow + 7 && column >= startColumn && column < startColumn + 7;
      const inFinder = finder(0, 0) || finder(0, size - 7) || finder(size - 7, 0);
      if (inFinder) {
        const startRow = row < 7 ? 0 : size - 7;
        const startColumn = column < 7 ? 0 : size - 7;
        const finderRow = row - startRow;
        const finderColumn = column - startColumn;
        return finderRow === 0 || finderRow === 6 || finderColumn === 0 || finderColumn === 6 || (finderRow >= 2 && finderRow <= 4 && finderColumn >= 2 && finderColumn <= 4);
      }
      return ((row * 17 + column * 31 + hash + row * column) % 7) < 3;
    }));
  }, [value]);

  return <svg className="qr-svg" viewBox={`0 0 ${size} ${size}`} role="img" aria-label="Generated attendance QR code"><rect width={size} height={size} fill="white" />{modules.flatMap((row, rowIndex) => row.map((dark, columnIndex) => dark ? <rect key={`${rowIndex}-${columnIndex}`} x={columnIndex} y={rowIndex} width="1" height="1" fill="#17100b" /> : null))}</svg>;
}
