import { useEffect, useState } from "react";
import { zonesApi } from "../../zones/api/zonesApi";

export default function ComplianceFilters({ value, onChange }) {
  const [zones, setZones] = useState([]);

  useEffect(() => {
    zonesApi.list().then(setZones).catch(() => setZones([]));
  }, []);

  if (!value) return null;

  const update = (patch) => onChange({ ...value, ...patch });

  return (
    <div className="flex flex-wrap gap-3 items-end mb-4">
      <div>
        <label className="block text-xs text-gray-500">Zona</label>
        <select
          className="border rounded px-2 py-1 text-sm"
          value={value.zoneId}
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
          value={value.from} onChange={(e) => update({ from: e.target.value })} />
      </div>
      <div>
        <label className="block text-xs text-gray-500">Hasta</label>
        <input type="date" className="border rounded px-2 py-1 text-sm"
          value={value.to} onChange={(e) => update({ to: e.target.value })} />
      </div>
    </div>
  );
}