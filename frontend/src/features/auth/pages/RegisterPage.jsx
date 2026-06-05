import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { authApi } from "../api/authApi";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../../../shared/ui/useToast";
import PasswordStrength from "../components/PasswordStrength";
import { isPasswordValid } from "../domain/password";
import { Rombo } from "@/ui/Rombo";
import { Radar } from "@/ui/Rings";

const C = {
  petroleo: "#13694A",
  naranja: "#F0A02A",
  verde: "#8FBE3D",
};
const FD = "Sora, sans-serif";
const FB = "Manrope, sans-serif";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function Eye({ open }) {
  return open ? (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  ) : (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 3l18 18M10.6 10.7a3 3 0 0 0 4.2 4.2M9.4 5.2A9.7 9.7 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3 3.8M6.1 6.2A17 17 0 0 0 2 12s3.5 7 10 7a9.6 9.6 0 0 0 3.3-.6" />
    </svg>
  );
}

function FieldA({ label, type = "text", value, onChange, placeholder, autoComplete, autoFocus, trailing }) {
  const [foc, setFoc] = useState(false);
  return (
    <label style={{ display: "block" }}>
      <span style={{ display: "block", fontFamily: FB, fontSize: 13, fontWeight: 600, color: "#41524a", marginBottom: 7 }}>
        {label}
      </span>
      <div style={{
        position: "relative", display: "flex", alignItems: "center", background: "#fff",
        borderRadius: 12,
        border: `1.5px solid ${foc ? C.petroleo : "rgba(19,105,74,0.18)"}`,
        boxShadow: foc ? "0 0 0 4px rgba(19,105,74,0.10)" : "0 1px 2px rgba(20,40,30,0.04)",
        transition: "border-color .15s, box-shadow .15s",
      }}>
        <input
          type={type} value={value} placeholder={placeholder}
          autoComplete={autoComplete} autoFocus={autoFocus}
          onChange={(e) => onChange(e.target.value)}
          onFocus={() => setFoc(true)} onBlur={() => setFoc(false)}
          style={{ flex: 1, border: "none", outline: "none", background: "transparent", padding: "13px 14px", fontFamily: FB, fontSize: 15.5, color: "#16261f", borderRadius: 12 }}
        />
        {trailing}
      </div>
    </label>
  );
}

export default function RegisterPage() {
  const { isAuthenticated, loading } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [show, setShow] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (loading) return null;
  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  function validate() {
    if (!fullName.trim()) return "Ingresa tu nombre completo.";
    if (!EMAIL_RE.test(email.trim())) return "El correo no tiene un formato válido.";
    if (!isPasswordValid(password))
      return "La contraseña debe tener mínimo 10 caracteres, una mayúscula, una minúscula y un dígito.";
    if (password !== confirm) return "Las contraseñas no coinciden.";
    return "";
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const v = validate();
    if (v) { setError(v); return; }
    setError("");
    setSubmitting(true);
    try {
      await authApi.register({
        email: email.trim(),
        password,
        fullName: fullName.trim(),
      });
      toast.success(
        "Cuenta creada. Tu rol inicial es VIEWER. Pide a un administrador que ajuste tu rol si necesitas más permisos."
      );
      navigate("/login", { replace: true });
    } catch (err) {
      const status = err.response?.status;
      const msg = err.response?.data?.message;
      const code = msg?.split(":")[0];
      if (status === 409 || code === "EMAIL_TAKEN") {
        setError("Ya existe una cuenta con este correo.");
      } else if (status === 429 || code === "RATE_LIMITED") {
        setError("Demasiados intentos. Espera unos minutos.");
      } else if (status === 400) {
        setError(msg || "Revisa los datos ingresados.");
      } else {
        setError(msg || "No se pudo crear la cuenta. Intenta de nuevo.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div style={{
      position: "relative", overflow: "hidden",
      minHeight: "100vh", width: "100%",
      background: "radial-gradient(120% 120% at 18% 0%, #1a7a58 0%, #0f5840 45%, #0a3327 100%)",
      display: "flex", alignItems: "center", justifyContent: "center",
      padding: "32px 18px",
    }}>
      {/* Pulso radar animado de fondo */}
      <div style={{ position: "absolute", top: "-8%", left: "-6%", pointerEvents: "none", opacity: 0.45 }}>
        <Radar color={C.verde} size={420} count={4} />
      </div>
      <div style={{ position: "absolute", bottom: "-12%", right: "-8%", pointerEvents: "none", opacity: 0.35 }}>
        <Radar color={C.verde} size={360} count={3} />
      </div>

      <div style={{
        position: "relative", zIndex: 1,
        width: "100%", maxWidth: 440, background: "#F7F5EF",
        borderRadius: 20, padding: "38px clamp(26px, 5vw, 44px)",
        boxShadow: "0 24px 60px rgba(10,40,30,0.35)",
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 11, marginBottom: 26 }}>
          <Rombo size={36} gap="#F7F5EF" />
          <div>
            <div style={{ fontFamily: FD, fontWeight: 700, fontSize: 19, color: "#13231c", letterSpacing: "-0.01em" }}>
              AcústicaUPC
            </div>
            <div style={{ fontFamily: FB, fontSize: 11, fontWeight: 600, letterSpacing: "0.12em", textTransform: "uppercase", color: "#8a978f" }}>
              Crear cuenta
            </div>
          </div>
        </div>

        <h1 style={{ fontFamily: FD, fontWeight: 700, fontSize: 26, color: "#13231c", letterSpacing: "-0.02em", margin: "0 0 6px" }}>
          Regístrate
        </h1>
        <p style={{ fontFamily: FB, fontSize: 14.5, color: "#5a6962", margin: "0 0 26px" }}>
          Crea tu cuenta para acceder al panel de monitoreo.
        </p>

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
          <FieldA
            label="Nombre completo"
            value={fullName}
            onChange={setFullName}
            placeholder="Tu nombre"
            autoComplete="name"
            autoFocus
          />
          <FieldA
            label="Correo"
            type="email"
            value={email}
            onChange={setEmail}
            placeholder="correo@ejemplo.com"
            autoComplete="email"
          />
          <div>
            <FieldA
              label="Contraseña"
              type={show ? "text" : "password"}
              value={password}
              onChange={setPassword}
              placeholder="••••••••"
              autoComplete="new-password"
              trailing={
                <button
                  type="button"
                  onClick={() => setShow((s) => !s)}
                  aria-label="Mostrar contraseña"
                  style={{ border: "none", background: "transparent", cursor: "pointer", padding: "0 13px", display: "flex", alignItems: "center", color: "#7c8c84" }}
                >
                  <Eye open={show} />
                </button>
              }
            />
            <PasswordStrength value={password} />
          </div>
          <FieldA
            label="Confirmar contraseña"
            type={show ? "text" : "password"}
            value={confirm}
            onChange={setConfirm}
            placeholder="••••••••"
            autoComplete="new-password"
          />

          {error && (
            <div style={{ fontFamily: FB, fontSize: 13.5, color: "#c0492a", fontWeight: 600 }}>
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            style={{
              marginTop: 4, border: "none", cursor: submitting ? "default" : "pointer",
              borderRadius: 12, padding: 15,
              background: submitting ? "#2f6b53" : C.petroleo,
              color: "#fff", fontFamily: FD, fontWeight: 600, fontSize: 16,
              boxShadow: "0 8px 20px rgba(19,105,74,0.28)",
              display: "flex", alignItems: "center", justifyContent: "center", gap: 10,
            }}
          >
            {submitting ? (
              <>
                <span style={{ width: 16, height: 16, border: "2.5px solid rgba(255,255,255,0.4)", borderTopColor: "#fff", borderRadius: 99, animation: "acu-spin .7s linear infinite" }} />
                Creando cuenta…
              </>
            ) : (
              "Crear cuenta"
            )}
          </button>
        </form>

        <p style={{ fontFamily: FB, fontSize: 13.5, textAlign: "center", color: "#5a6962", marginTop: 22 }}>
          ¿Ya tienes cuenta?{" "}
          <Link to="/login" style={{ color: C.petroleo, fontWeight: 700, textDecoration: "underline" }}>
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  );
}
