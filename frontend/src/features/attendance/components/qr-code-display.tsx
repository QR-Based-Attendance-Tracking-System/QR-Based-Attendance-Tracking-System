"use client";

import { QRCodeSVG } from "qrcode.react";

type QRCodeDisplayProps = {
  token: string;
};

/** Renders a QR code for an opaque token supplied by the caller. */
export function QRCodeDisplay({ token }: QRCodeDisplayProps) {
  return (
    <QRCodeSVG
      className="qr-svg"
      value={token}
      size={512}
      level="M"
      marginSize={4}
      bgColor="#ffffff"
      fgColor="#17100b"
      title="Attendance session QR code"
    />
  );
}
