import { useEffect, useState } from "react";
import { zonesApi } from "../../zones/api/zonesApi";

/**
 * Devuelve el rango "los ultimos 30 dias"
 * como ISO con offset -05:00.
 **/

function defaultRange() {
  const to = new Date();
  const from = new Date(to.getTime() - 30 * 24 * 60 * 60 * 1000);
  return {
    from: from.toISOString().slice(0, 10),  // YYYY-MM-DD
    to:   to.toISOString().slice(0, 10),
  };
}

export default function ComplianceFilters({ value, onChange }) {
  const [zones, setZones] = useState([]);
  const range = defaultRange();
  const v = value ?? { zoneId: "", from: range.from, to: range.to };

  useEffect(() => {
    zonesApi.list().then(setZones).catch(() => setZones([]));
  }, []);

  function update(patch) {
    onChange({ ...v, ...patch });
  }

  return (
    <div className="flex flex-wrap gap-3 items-end mb-4">
      <div>
        <label className="block text-xs text-gray-500">Zona</label>
        <select
          className="border rounded px-2 py-1 text-sm"
          value={v.zoneId}
          onChange={(e) => update({ zoneId: e.target.value })}
        >
          <option value="">Todas</option>
          {zones.map((z) => (
            <option key={z.id} value={z.id}>{z.name}</option>
          ))}
        </select>
      </div>
      <div>
        <label className="block text-xs text-gray-500">Desde</label>
        <input type="date" className="border rounded px-2 py-1 text-sm"
          value={v.from} onChange={(e) => update({ from: e.target.value })} />
      </div>
      <div>
        <label className="block text-xs text-gray-500">Hasta</label>
        <input type="date" className="border rounded px-2 py-1 text-sm"
          value={v.to} onChange={(e) => update({ to: e.target.value })} />
      </div>
    </div>
  );
}