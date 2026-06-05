import { useEffect, useState } from "react";

/** Barra de progreso animada (para el % de cumplimiento). */
export default function ProgressBar({ pct = 0 }) {
  const [w, setW] = useState(0);
  useEffect(() => {
    const target = Math.max(0, Math.min(100, pct));
    const t = setTimeout(() => setW(target), 200);
    return () => clearTimeout(t);
  }, [pct]);

  return (
    <div className="h-[7px] overflow-hidden rounded-full bg-petroleo/10">
      <div
        className="h-full rounded-full"
        style={{
          width: `${w}%`,
          background: "linear-gradient(90deg, #8FBE3D, #13694A)",
          transition: "width 1.1s cubic-bezier(.2,1.5,.4,1)",
        }}
      />
    </div>
  );
}
