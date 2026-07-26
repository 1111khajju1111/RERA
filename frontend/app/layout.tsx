import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "AI RERA Auditor",
  description: "From Blueprint to Approval — Powered by Artificial Intelligence.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className="dark">
      <body>{children}</body>
    </html>
  );
}
