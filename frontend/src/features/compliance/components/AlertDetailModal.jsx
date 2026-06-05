import Modal from "../../../shared/ui/Modal";
import AlertSeverityBadge from "./AlertSeverityBadge";
import MitigationActions from "./MitigationActions";
import BatchObservationNote from "../../ingestion/components/BatchObservationNote";

const PERIOD_LABEL = { DIURNO: "Diurno", NOCTURNO: "Nocturno" };
const fmt = (iso) =>
  iso ? new Date(iso).toLocaleString("es-CO", { dateStyle: "medium", timeStyle: "short" }) : "—";

export default function AlertDetailModal({ alert, open, onClose }) {
  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Detalle de alerta"
      width={580}
      footer={
        <button
          type="button"
          onClick={onClose}
          className="rounded-[11px] border-[1.5px] border-petroleo/15 px-4 py-2 text-sm font-semibold text-petroleo hover:bg-petroleo/[0.06]"
        >
          Cerrar
        </button>
      }
    >
      {alert && (
        <div className="space-y-5">
          <div className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
            <Info label="Zona" value={alert.zoneName} />
            <Info label="Disparada" value={fmt(alert.triggeredAt ?? alert.when)} />
            <Info label="Periodo" value={PERIOD_LABEL[alert.period] ?? alert.period} />
            <div>
              <dt className="text-xs uppercase tracking-wider text-slate-500">Severidad</dt>
              <dd className="mt-0.5"><AlertSeverityBadge severity={alert.severity} /></dd>
            </div>
            <Info label="Medido" value={`${Number(alert.measuredDb).toFixed(2)} dB`} />
            <Info label="Estándar" value={`${Number(alert.standardDb).toFixed(2)} dB`} />
            <Info label="Exceso" value={`+${Number(alert.excessDb).toFixed(2)} dB`} valueClass="text-rose-700 font-semibold" />
          </div>

          <div>
            <h3 className="font-semibold text-slate-800 mb-2">Observación de la carga</h3>
            <BatchObservationNote batchId={alert.batchId} />
          </div>

          <div>
            <h3 className="font-semibold text-slate-800 mb-2">Acciones recomendadas</h3>
            <MitigationActions excessDb={alert.excessDb} />
          </div>
        </div>
      )}
    </Modal>
  );
}

function Info({ label, value, valueClass = "text-slate-800" }) {
  return (
    <div>
      <dt className="text-xs uppercase tracking-wider text-slate-500">{label}</dt>
      <dd className={`mt-0.5 ${valueClass}`}>{value}</dd>
    </div>
  );
}
