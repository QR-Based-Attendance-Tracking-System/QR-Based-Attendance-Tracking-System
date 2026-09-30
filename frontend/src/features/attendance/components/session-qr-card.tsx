import { QrCode } from "@/features/attendance/components/qr-code";

type SessionQrCardProps = { qrToken: string; sessionId: string; isActive: boolean; onEnd: () => void };

export function SessionQrCard({ qrToken, sessionId, isActive, onEnd }: SessionQrCardProps) {
  return <div className="qr-card"><h3>Session QR Code</h3><div className="qr-frame"><QrCode value={qrToken} /></div><strong className="session-code">ID-{sessionId}</strong>{isActive && <span className="live-label"><i /> Session active · Students can check in</span>}<button className="end-button" onClick={onEnd} disabled={!isActive}>End Session</button></div>;
}
