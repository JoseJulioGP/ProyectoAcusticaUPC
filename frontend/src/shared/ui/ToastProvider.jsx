import { useCallback, useMemo, useRef, useState } from "react";
import { ToastContext } from "./ToastContext";

/**
 * Sistema de toasts minimalista para el proyecto (sin librerías nuevas).
 * Montar <ToastProvider> por encima del router; el hook useToast() (en
 * ./useToast) expone success/error/info. Los toasts sobreviven a la
 * navegación porque el provider vive por encima de <Routes>.
 */

const VARIANTS = {
  success: { bar: "#13694A", icon: "✓" },
  error: { bar: "#c0492a", icon: "!" },
  info: { bar: "#2E93C9", icon: "i" },
};

export function ToastProvider({ children, duration = 5000 }) {
  const [toasts, setToasts] = useState([]);
  const idRef = useRef(0);

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback(
    (variant, message) => {
      const id = ++idRef.current;
      setToasts((prev) => [...prev, { id, variant, message }]);
      if (duration > 0) {
        setTimeout(() => dismiss(id), duration);
      }
      return id;
    },
    [duration, dismiss]
  );

  const api = useMemo(
    () => ({
      success: (msg) => push("success", msg),
      error: (msg) => push("error", msg),
      info: (msg) => push("info", msg),
      dismiss,
    }),
    [push, dismiss]
  );

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div
        style={{
          position: "fixed",
          top: 20,
          right: 20,
          zIndex: 9999,
          display: "flex",
          flexDirection: "column",
          gap: 10,
          maxWidth: 380,
          pointerEvents: "none",
        }}
      >
        {toasts.map((t) => {
          const v = VARIANTS[t.variant] ?? VARIANTS.info;
          return (
            <div
              key={t.id}
              role="status"
              onClick={() => dismiss(t.id)}
              style={{
                pointerEvents: "auto",
                cursor: "pointer",
                display: "flex",
                alignItems: "flex-start",
                gap: 12,
                background: "#fff",
                borderRadius: 12,
                borderLeft: `4px solid ${v.bar}`,
                boxShadow: "0 10px 30px rgba(20,40,30,0.18)",
                padding: "13px 16px",
                fontFamily: "Manrope, sans-serif",
                fontSize: 14,
                color: "#16261f",
                lineHeight: 1.4,
                animation: "acu-toast-in .22s ease-out",
              }}
            >
              <span
                style={{
                  flexShrink: 0,
                  width: 22,
                  height: 22,
                  borderRadius: 99,
                  background: v.bar,
                  color: "#fff",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: 13,
                  fontWeight: 700,
                }}
              >
                {v.icon}
              </span>
              <span style={{ flex: 1 }}>{t.message}</span>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}
