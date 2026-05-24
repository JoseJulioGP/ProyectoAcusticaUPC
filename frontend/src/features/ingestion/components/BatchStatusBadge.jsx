const STATUS_STYLES = {
  PENDING: "bg-slate-100 text-slate-700 ring-slate-200",
  PROCESSING: "bg-amber-100 text-amber-700 ring-amber-200 animate-pulse",
  COMPLETED: "bg-emerald-100 text-emerald-700 ring-emerald-200",
  FAILED: "bg-rose-100 text-rose-700 ring-rose-200",
};

const STATUS_LABELS = {
  PENDING: "Pendiente",
  PROCESSING: "Procesando…",
  COMPLETED: "Completado",
  FAILED: "Falló",
};

export default function BatchStatusBadge({ status }) {
  const style = STATUS_STYLES[status] ?? STATUS_STYLES.PENDING;
  const label = STATUS_LABELS[status] ?? status;
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${style}`}
    >
      {label}
    </span>
  );
}