package lk.ruhunaefac.qrattendance.attendance.dto;

import java.time.Instant;

public record QrChallengeResponse(String qrToken, String code, Instant expiresAt, Instant serverTime) { }
