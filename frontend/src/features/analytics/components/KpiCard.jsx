import { useState } from "react";
import { Card, CountUp } from "@/ui/primitives";

const ACCENT = {
  slate: "#13694A",
  petroleo: "#13694A",
  blue: "#2E93C9",
  green: "#8FBE3D",
  amber: "#F0A02A",
  red: "#cf4f2c",
};

export default function KpiCard({
  label,
  value,
  decimals = 0,
  suffix = "",
  sub,
  footer,
  accent = "slate",
  loading = false,
}) {
  const color = ACCENT[accent] ?? ACCENT.slate;
  const [hover, setHover] = useState(false);
  const isNum = typeof value === "number";

  return (
    <Card accent={color} lift>
      <div
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
        className="relative overflow-hidden"
      >
        {/* glow de acento al hover */}
        <div
          className="pointer-events-none absolute -inset-5 transition-opacity duration-300"
          style={{
            opacity: hover ? 1 : 0,
            background: `radial-gradient(120% 120% at 100% 0%, ${color}22, transparent 60%)`,
          }}
        />
        <div className="relative">
          <p className="mb-3 font-body text-[11.5px] font-bold uppercase tracking-[0.1em] text-muted">
            {label}
          </p>
          {loading ? (
            <div className="h-9 w-24 animate-pulse rounded bg-slate-100" />
          ) : (
            <p className="font-display text-[40px] font-bold leading-none tracking-[-0.02em] text-ink">
              {isNum ? <CountUp value={value} decimals={decimals} suffix={suffix} /> : value}
            </p>
          )}
          <div className="mt-3 min-h-[18px]">
            {footer ?? (sub ? <span className="font-body text-xs text-muted">{sub}</span> : null)}
          </div>
        </div>
      </div>
    </Card>
  );
}
