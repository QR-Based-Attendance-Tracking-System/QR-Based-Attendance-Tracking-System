import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    const apiServer = process.env.ATTENDANCE_API_INTERNAL_URL ?? "http://localhost:8080";
    return [
      {
        source: "/api/:path*",
        destination: `${apiServer}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
