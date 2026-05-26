import AlertSeverityBadge from "./AlertSeverityBadge";

function formatDateTime(iso) {
  return new Date(iso).toLocaleString("es-CO", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

export default function AlertsTable({ alerts }) {
  if (!alerts || alerts.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        No hay alertas en el rango seleccionado.
      </div>
    );
  }
  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="text-left text-xs uppercase text-gray-500 border-b">
          <th className="py-2">Disparada</th>
          <th>Zona</th>
          <th>Periodo</th>
          <th className="text-right">Medido (dB)</th>
          <th className="text-right">Estandar (dB)</th>
          <th className="text-right">Exceso (dB)</th>
          <th>Severidad</th>
        </tr>
      </thead>
      <tbody>
        {alerts.map((a) => (
          <tr key={a.id} className="border-b hover:bg-gray-50">
            <td className="py-2">{formatDateTime(a.triggeredAt)}</td>
            <td>{a.zoneName}</td>
            <td>{a.period}</td>
            <td className="text-right font-mono">{Number(a.measuredDb).toFixed(2)}</td>
            <td className="text-right font-mono">{Number(a.standardDb).toFixed(2)}</td>
            <td className="text-right font-mono font-semibold text-red-700">
              +{Number(a.excessDb).toFixed(2)}
            </td>
            <td><AlertSeverityBadge severity={a.severity} /></td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}