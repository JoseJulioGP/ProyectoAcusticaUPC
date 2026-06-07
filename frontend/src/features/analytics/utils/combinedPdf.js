import apiClient from "../../../shared/api/apiClient";

const A4_W = 595.28; // pt (A4 vertical)
const A4_H = 841.89;
const MARGIN = 24;

function isoDate(v) {
  if (!v) return v;
  return typeof v === "string" ? v.slice(0, 10) : v;
}

function dataUrlToUint8(dataUrl) {
  const base64 = dataUrl.split(",")[1];
  const bin = atob(base64);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  return bytes;
}

/** Descarga (solo) el PDF de cumplimiento del backend como blob. */
export async function fetchComplianceReportBlob({ from, to, zoneId }) {
  const params = new URLSearchParams();
  if (from) params.set("from", isoDate(from));
  if (to) params.set("to", isoDate(to));
  if (zoneId) params.set("zoneId", zoneId);
  const res = await apiClient.get(`/reports/compliance/pdf?${params.toString()}`, {
    responseType: "blob",
  });
  return res.data; // Blob
}

/**
 * Construye un PDF que junta, en orden:
 *   1. El reporte de cumplimiento del backend (si includeReport).
 *   2. La captura del dashboard (html2canvas), sección por sección.
 *
 * `reportBlob` permite pasar el reporte ya pre-descargado (para que el clic de
 * compartir sea rápido y abra el menú del sistema sin perder el gesto).
 * Devuelve { blob, filename }.
 */
export async function buildCombinedPdf({
  node,
  from,
  to,
  zoneId,
  includeReport = true,
  reportBlob = null,
  scale = 2,
}) {
  const [{ PDFDocument }, { default: html2canvas }] = await Promise.all([
    import("pdf-lib"),
    import("html2canvas"),
  ]);

  // 1) Reporte del backend (primero).
  let pdfDoc;
  let filename = "dashboard.pdf";
  if (includeReport) {
    try {
      const blob = reportBlob ?? (await fetchComplianceReportBlob({ from, to, zoneId }));
      filename = "reporte-dashboard.pdf";
      pdfDoc = await PDFDocument.load(await blob.arrayBuffer());
    } catch {
      pdfDoc = await PDFDocument.create(); // backend no disponible → solo dashboard
    }
  } else {
    pdfDoc = await PDFDocument.create();
  }

  // 2) Captura del dashboard, sección por sección (sin cortar tarjetas).
  const usableW = A4_W - MARGIN * 2;
  const usableH = A4_H - MARGIN * 2;
  const opts = {
    scale,
    backgroundColor: "#ffffff",
    useCORS: true,
    onclone: (doc) => {
      const style = doc.createElement("style");
      style.textContent =
        "*{animation:none !important;transition:none !important}" +
        ".acu-stagger>*{opacity:1 !important;transform:none !important}";
      doc.head.appendChild(style);
    },
  };

  const sections = Array.from(node.children).filter(
    (el) => !el.hasAttribute("data-html2canvas-ignore") && el.offsetHeight > 4
  );

  let page = pdfDoc.addPage([A4_W, A4_H]);
  let cursorY = MARGIN; // medido desde arriba

  for (const section of sections) {
    const canvas = await html2canvas(section, opts);
    const png = await pdfDoc.embedPng(dataUrlToUint8(canvas.toDataURL("image/png")));

    let drawW = usableW;
    let drawH = (canvas.height * drawW) / canvas.width;
    if (drawH > usableH) {
      const k = usableH / drawH;
      drawH = usableH;
      drawW = usableW * k;
    }

    if (cursorY + drawH > A4_H - MARGIN && cursorY > MARGIN) {
      page = pdfDoc.addPage([A4_W, A4_H]);
      cursorY = MARGIN;
    }

    const x = MARGIN + (usableW - drawW) / 2;
    const y = A4_H - cursorY - drawH; // pdf-lib usa origen abajo-izquierda
    page.drawImage(png, { x, y, width: drawW, height: drawH });
    cursorY += drawH + 12;
  }

  const bytes = await pdfDoc.save();
  const blob = new Blob([bytes], { type: "application/pdf" });
  return { blob, filename };
}
