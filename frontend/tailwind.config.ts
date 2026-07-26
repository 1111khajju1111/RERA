import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: "class",
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        background: "#05070f",
        surface: "#0b0e1a",
        border: "rgba(255,255,255,0.08)",
        brand: {
          blue: "#3b82f6",
          purple: "#a855f7",
          cyan: "#22d3ee",
        },
        severity: {
          critical: "#ef4444",
          major: "#f59e0b",
          minor: "#eab308",
        },
      },
      backgroundImage: {
        "brand-gradient": "linear-gradient(135deg, #3b82f6 0%, #a855f7 50%, #22d3ee 100%)",
      },
      boxShadow: {
        glow: "0 0 40px -10px rgba(99, 102, 241, 0.4)",
      },
    },
  },
  plugins: [],
};

export default config;
