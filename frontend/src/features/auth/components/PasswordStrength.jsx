import { passwordChecks } from "../domain/password";

/**
 * Indicador visual de fortaleza de contraseña.
 */

const LEVELS = [
  { label: "Muy débil", color: "#c0492a" },
  { label: "Débil", color: "#d8552f" },
  { label: "Aceptable", color: "#F0A02A" },
  { label: "Buena", color: "#8FBE3D" },
  { label: "Fuerte", color: "#13694A" },
];

export default function PasswordStrength({ value = "" }) {
  const checks = passwordChecks(value);
  const score = Object.values(checks).filter(Boolean).length; // 0..4
  const level = value ? LEVELS[score] : null;

  return (
    <div style={{ marginTop: 8 }}>
      <div style={{ display: "flex", gap: 4 }}>
        {[0, 1, 2, 3].map((i) => (
          <div
            key={i}
            style={{
              flex: 1,
              height: 5,
              borderRadius: 99,
              background:
                value && i < score ? level.color : "rgba(19,105,74,0.12)",
              transition: "background .2s",
            }}
          />
        ))}
      </div>
      {value && (
        <div
          style={{
            marginTop: 7,
            fontFamily: "Manrope, sans-serif",
            fontSize: 12,
            color: "#5a6962",
            display: "flex",
            flexWrap: "wrap",
            gap: "2px 12px",
          }}
        >
          <span style={{ color: level.color, fontWeight: 700 }}>
            {level.label}
          </span>
          <Req ok={checks.length}>10+ caracteres</Req>
          <Req ok={checks.upper}>1 mayúscula</Req>
          <Req ok={checks.lower}>1 minúscula</Req>
          <Req ok={checks.digit}>1 dígito</Req>
        </div>
      )}
    </div>
  );
}

function Req({ ok, children }) {
  return (
    <span style={{ color: ok ? "#13694A" : "#a9a293", fontWeight: 600 }}>
      {ok ? "✓" : "○"} {children}
    </span>
  );
}
