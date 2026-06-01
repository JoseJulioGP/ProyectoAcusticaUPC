import { useEffect, useState } from "react";
import { useDashboardFilter } from "../context/DashboardFilterContext";
import apiClient from "../../../shared/api/apiClient";

const presets = [
  { id: "7d", label: "Últimos 7 días" },
  { id: "30d", label: "Últimos 30 días" },
  { id: "1y", label: "Último año" },
];

const periods = [
  { id: null, label: "Diurno y nocturno" },
  { id: "DIURNO", label: "Diurno" },
  { id: "NOCTURNO", label: "Nocturno" },
];

export default function GlobalFilters() {
  const { from, to, zoneId, period, setFrom, setTo, setZoneId, setPeriod, setPreset } =
    useDashboardFilter();
  const [zones, setZones] = useState([]);

  useEffect(() => {
    apiClient.get("/zones").then((res) => setZones(res.data));
  }, []);

  return (
    <div className="bg-white rounded-lg shadow-sm border border-slate-200 p-4 mb-6">
      <div className="flex flex-wrap gap-4 items-end">
        <div>
          <label className="block text-xs font-medium text-slate-600 mb-1">Desde</label>
          <input
            type="datetime-local"
            className="border border-slate-300 rounded px-2 py-1 text-sm"
            value={from.slice(0, 16)}
            onChange={(e) => setFrom(new Date(e.target.value).toISOString())}
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-slate-600 mb-1">Hasta</label>
          <input
            type="datetime-local"
            className="border border-slate-300 rounded px-2 py-1 text-sm"
            value={to.slice(0, 16)}
            onChange={(e) => setTo(new Date(e.target.value).toISOString())}
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-slate-600 mb-1">Zona</label>
          <select
            className="border border-slate-300 rounded px-2 py-1 text-sm"
            value={zoneId ?? ""}
            onChange={(e) => setZoneId(e.target.value || null)}
          >
            <option value="">Todas las zonas</option>
            {zones.map((z) => (
              <option key={z.id} value={z.id}>
                {z.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-slate-600 mb-1">Período</label>
          <select
            className="border border-slate-300 rounded px-2 py-1 text-sm"
            value={period ?? ""}
            onChange={(e) => setPeriod(e.target.value || null)}
          >
            {periods.map((p) => (
              <option key={p.id ?? "all"} value={p.id ?? ""}>
                {p.label}
              </option>
            ))}
          </select>
        </div>
        <div className="flex gap-2 items-end">
          {presets.map((p) => (
            <button
              key={p.id}
              onClick={() => setPreset(p.id)}
              className="text-xs px-2 py-1 rounded border border-slate-300 hover:bg-slate-50"
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}