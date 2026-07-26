/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: "standalone", // Phase 10: produces a minimal self-contained server for the Docker image
};

export default nextConfig;
