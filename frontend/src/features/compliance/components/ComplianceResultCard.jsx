import ComplianceStatusBadge from "./ComplianceStatusBadge";

export default function ComplianceResultCard({ result }) {
  const r = result;
  return (
    <div className="border rounded-lg p-4 bg-white shadow-sm">
      <div className="flex justify-between items-start mb-3">
        <div>
          <h3 className="font-semibold text-lg">Periodo {r.period}</h3>
          <p className="text-xs text-gray-500">
            {r.measurementCount} mediciones · estandar {r.standardType}
          </p>
        </div>
        <ComplianceStatusBadge status={r.status} />
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
        <Metric label="LAeq" value={Number(r.laeqDb).toFixed(2)} unit="dB"
          highlight={r.status === "NO_CUMPLE"} />
        <Metric label="L90"  value={Number(r.l90Db).toFixed(2)}  unit="dB" />
        <Metric label="Min"  value={Number(r.minDb).toFixed(2)}  unit="dB" />
        <Metric label="Max"  value={Number(r.maxDb).toFixed(2)}  unit="dB" />
      </div>
      <div className="mt-3 pt-3 border-t text-xs text-gray-600 flex justify-between">
        <span>Estandar Res. 627: <b>{Number(r.standardDb).toFixed(2)} dB</b></span>
        <span className={r.excessDb > 0 ? "text-red-700 font-semibold" : "text-emerald-700"}>
          {r.excessDb > 0 ? `Excede en +${Number(r.excessDb).toFixed(2)} dB` :
                             `Por debajo en ${Number(r.excessDb).toFixed(2)} dB`}
        </span>
      </div>
    </div>
  );
}

function Metric({ label, value, unit, highlight }) {
  return (
    <div>
      <div className="text-xs text-gray-500">{label}</div>
      <div className={`text-xl font-mono ${highlight ? "text-red-700 font-bold" : ""}`}>
        {value} <span className="text-xs text-gray-500">{unit}</span>
      </div>
    </div>
  );
}