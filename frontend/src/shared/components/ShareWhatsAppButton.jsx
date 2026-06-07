import { MessageCircle } from "lucide-react";

/**
 * Comparte por WhatsApp con un deep link wa.me. Si se pasa `message`, comparte
 * ese texto (p. ej. los KPIs que muestra el dashboard); si no, un mensaje
 * genérico con la URL actual. Abre WhatsApp (web o app) pre-llenado.
 */
export default function ShareWhatsAppButton({ message }) {
  const onClick = () => {
    const text = encodeURIComponent(
      message ?? `AcústicaUPC — avance del proyecto: ${window.location.href}`
    );
    window.open(`https://wa.me/?text=${text}`, "_blank", "noopener,noreferrer");
  };

  return (
    <button
      type="button"
      onClick={onClick}
      aria-label="Compartir por WhatsApp"
      title="Compartir por WhatsApp"
      className="flex items-center gap-1.5 px-2 md:px-3 py-1.5 text-sm font-body text-ink2 hover:text-petroleo hover:bg-petroleo/10 rounded-lg transition-colors"
    >
      <MessageCircle size={16} />
      <span className="hidden sm:inline">Compartir</span>
    </button>
  );
}
