import { useState, useEffect } from "react";
import { Share2, Loader2, FileDown } from "lucide-react";
import { useDashboardFilter } from "../context/DashboardFilterContext";
import { buildCombinedPdf } from "../utils/combinedPdf";
import { useToast } from "../../../shared/ui/useToast";

/**
 * Compartir el PDF combinado (reporte del backend + captura del dashboard)
 * abriendo el menú de compartir del sistema (Windows / móvil).
 *
 * En DOS pasos a propósito: generar el combinado tarda (red + unión de PDFs) y
 * eso "consume" el gesto del clic, impidiendo abrir navigator.share. Con el
 * archivo ya preparado, el 2º clic abre el menú al instante.
 */
export default function ShareDashboardPdf({ targetRef, message }) {
  const { from, to, zoneId } = useDashboardFilter();
  const toast = useToast();
  const [preparing, setPreparing] = useState(false);
  const [file, setFile] = useState(null);

  // Filtros nuevos → el PDF preparado queda obsoleto.
  useEffect(() => {
    setFile(null);
  }, [from, to, zoneId]);

  const prepare = async () => {
    const node = targetRef?.current;
    if (!node || preparing) return;
    setPreparing(true);
    try {
      const { blob, filename } = await buildCombinedPdf({ node, from, to, zoneId });
      setFile(new File([blob], filename, { type: "application/pdf" }));
    } catch {
      toast.error("No se pudo generar el PDF.");
    } finally {
      setPreparing(false);
    }
  };

  const share = async () => {
    if (!file) return;
    // Menú de compartir del sistema (Windows / móvil) — clic limpio, archivo listo.
    if (navigator.canShare && navigator.canShare({ files: [file] })) {
      try {
        await navigator.share({
          files: [file],
          text: message,
          title: "AcústicaUPC — Dashboard",
        });
        return;
      } catch (err) {
        if (err?.name === "AbortError") return; // el usuario cerró el menú
      }
    }
    // Fallback: descargar + WhatsApp con el texto.
    const url = window.URL.createObjectURL(file);
    const a = document.createElement("a");
    a.href = url;
    a.download = file.name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
    const text = encodeURIComponent(`${message}\n\n(Adjunta el PDF descargado “${file.name}”.)`);
    window.open(`https://wa.me/?text=${text}`, "_blank", "noopener,noreferrer");
    toast.success("PDF descargado. Adjúntalo en WhatsApp.");
  };

  const baseCls =
    "inline-flex items-center gap-2 rounded px-4 py-2 text-white disabled:opacity-50";

  if (!file) {
    return (
      <button
        type="button"
        onClick={prepare}
        disabled={preparing}
        className={`${baseCls} bg-petroleo hover:bg-petroleo/90`}
      >
        {preparing ? <Loader2 size={16} className="animate-spin" /> : <FileDown size={16} />}
        {preparing ? "Preparando PDF para compartir…" : "Preparar PDF para compartir"}
      </button>
    );
  }

  return (
    <button
      type="button"
      onClick={share}
      className={`${baseCls} bg-emerald-600 hover:bg-emerald-700`}
    >
      <Share2 size={16} />
      Compartir PDF
    </button>
  );
}
