import { useEffect, useMemo, useState } from "react";
import { MapContainer, TileLayer, CircleMarker, Popup, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { Card, PageHead, Select } from "@/ui/primitives";
import { zonesApi } from "../../zones/api/zonesApi";
import { analyticsApi } from "../api/analyticsApi";
import { useRole } from "../../auth/hooks/useRole";
import { useToast } from "../../../shared/ui/useToast";

// Universidad Popular del Cesar — sede Valledupar (aprox). Centro del mapa.
// Si el campus queda algo descentrado, ajusta estos valores [lat, lng].
const UPC_CENTER = [10.4566, -73.2492];
const DEFAULT_ZOOM = 17;

// mapX = longitud, mapY = latitud (reutilizamos los campos existentes de Zone).
const hasPos = (z) => z.mapX != null && z.mapY != null;

function ClickToPlace({ active, onPick }) {
  useMapEvents({ click: (e) => active && onPick(e.latlng) });
  return null;
}

export default function NoiseMapPage() {
  const { isAdmin } = useRole();
  const canEdit = isAdmin; // VIEWER ve en solo lectura
  const toast = useToast();

  const [zones, setZones] = useState([]);
  const [stats, setStats] = useState([]);
  const [placingId, setPlacingId] = useState("");

  const [filters, setFilters] = useState(() => {
    const to = new Date();
    const from = new Date(to.getTime() - 365 * 24 * 60 * 60 * 1000);
    return { from: from.toISOString().slice(0, 10), to: to.toISOString().slice(0, 10) };
  });

  const loadZones = () =>
    zonesApi
      .list()
      .then((d) => setZones(Array.isArray(d) ? d : d.content ?? []))
      .catch(() => setZones([]));

  useEffect(() => { loadZones(); }, []);
  useEffect(() => {
    analyticsApi
      .zonesStats({ from: filters.from + "T00:00:00-05:00", to: filters.to + "T23:59:59-05:00" })
      .then((d) => setStats(Array.isArray(d) ? d : []))
      .catch(() => setStats([]));
  }, [filters]);

  const statByZone = useMemo(() => {
    const m = {};
    stats.forEach((s) => { m[s.zoneId] = s; });
    return m;
  }, [stats]);

  const placed = zones.filter(hasPos);
  const unplaced = zones.filter((z) => !hasPos(z));

  const colorFor = (zoneId) => {
    const s = statByZone[zoneId];
    // Sin stats o sin mediciones en el rango → "sin datos" (no es CUMPLE real).
    if (!s || !s.measurements) return "#8a978f";
    return s.overallStatus === "CUMPLE" ? "#8FBE3D" : "#cf4f2c";
  };

  const handlePick = async ({ lat, lng }) => {
    const z = zones.find((x) => x.id === placingId);
    if (!z) return;
    try {
      await zonesApi.update(z.id, {
        name: z.name,
        description: z.description,
        building: z.building,
        floor: z.floor,
        sector: z.sector,
        subsector: z.subsector,
        active: z.active,
        mapX: lng,
        mapY: lat,
      });
      toast.success(`Posición guardada para ${z.name}.`);
      setPlacingId("");
      await loadZones();
    } catch (err) {
      toast.error(err.response?.data?.message || "No se pudo guardar la posición.");
    }
  };

  const handleRemovePos = async (z) => {
    if (!window.confirm(`¿Quitar "${z.name}" del mapa?`)) return;
    try {
      await zonesApi.update(z.id, {
        name: z.name,
        description: z.description,
        building: z.building,
        floor: z.floor,
        sector: z.sector,
        subsector: z.subsector,
        active: z.active,
        mapX: null,
        mapY: null,
      });
      toast.success(`${z.name} quitada del mapa.`);
      await loadZones();
    } catch (err) {
      toast.error(err.response?.data?.message || "No se pudo quitar la zona del mapa.");
    }
  };

  return (
    <div className="acu-stagger p-6 max-w-7xl mx-auto">
      <PageHead
        title="Mapa de ruido"
        sub="Zonas de la UPC ubicadas en el mapa, coloreadas por su cumplimiento en el rango seleccionado."
      />

      {/* Filtro de fechas: recolorea las zonas según el periodo */}
      <Card className="mb-4" pad={16}>
        <div className="flex flex-wrap items-end gap-3">
          <div>
            <label className="block text-xs font-medium text-slate-500 mb-1">Desde</label>
            <input
              type="date"
              value={filters.from}
              onChange={(e) => setFilters((f) => ({ ...f, from: e.target.value }))}
              className="rounded-lg border border-slate-300 px-2.5 py-1.5 text-sm text-slate-700 focus:border-petroleo focus:outline-none focus:ring-2 focus:ring-petroleo/20"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-500 mb-1">Hasta</label>
            <input
              type="date"
              value={filters.to}
              onChange={(e) => setFilters((f) => ({ ...f, to: e.target.value }))}
              className="rounded-lg border border-slate-300 px-2.5 py-1.5 text-sm text-slate-700 focus:border-petroleo focus:outline-none focus:ring-2 focus:ring-petroleo/20"
            />
          </div>
          <span className="text-xs text-slate-500">Los colores reflejan el cumplimiento en este rango.</span>
        </div>
      </Card>

      {canEdit && (
        <Card className="mb-4" pad={16}>
          <div className="flex flex-wrap items-center gap-3">
            <label className="text-sm font-semibold text-slate-700">Colocar zona</label>
            <div className="min-w-[240px]">
              <Select
                value={placingId}
                onChange={setPlacingId}
                options={[
                  { value: "", label: "— Elige una zona para ubicar —" },
                  ...zones.map((z) => ({ value: z.id, label: `${z.name}${hasPos(z) ? " ✓" : ""}` })),
                ]}
              />
            </div>
            {placingId && (
              <span className="text-sm text-petroleo">Haz clic en el mapa para ubicarla.</span>
            )}
          </div>
        </Card>
      )}

      <Card pad={0} style={{ overflow: "hidden" }}>
        <MapContainer center={UPC_CENTER} zoom={DEFAULT_ZOOM} minZoom={13} maxZoom={19} style={{ height: 540, width: "100%" }}>
          {/* Imagen satelital realista (sin API key) */}
          <TileLayer
            url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
            attribution="Tiles &copy; Esri — Source: Esri, Maxar, Earthstar Geographics"
            maxZoom={19}
          />
          {/* Etiquetas de calles y lugares por encima */}
          <TileLayer
            url="https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}"
            maxZoom={19}
          />
          <ClickToPlace active={!!placingId} onPick={handlePick} />
          {placed.map((z) => {
            const s = statByZone[z.id];
            const color = colorFor(z.id);
            return (
              <CircleMarker
                key={z.id}
                center={[z.mapY, z.mapX]}
                radius={12}
                pathOptions={{ color, fillColor: color, fillOpacity: 0.55, weight: 2 }}
              >
                <Popup>
                  <strong>{z.name}</strong>
                  <br />
                  {s && s.measurements ? (
                    <>
                      Estado: {s.overallStatus}
                      <br />
                      Día: {s.laeqDiurnoDb ?? "—"} / {s.standardDayDb} dB
                      <br />
                      Noche: {s.laeqNocturnoDb ?? "—"} / {s.standardNightDb} dB
                      <br />
                      {s.measurements.toLocaleString("es-CO")} mediciones · {s.alertsCount} alertas
                    </>
                  ) : (
                    "Sin datos de ruido en el rango"
                  )}
                  {canEdit && (
                    <>
                      <br />
                      <button
                        type="button"
                        onClick={() => handleRemovePos(z)}
                        className="mt-1 font-semibold text-rose-600 underline"
                      >
                        Quitar del mapa
                      </button>
                    </>
                  )}
                </Popup>
              </CircleMarker>
            );
          })}
        </MapContainer>
      </Card>

      {/* Leyenda */}
      <div className="mt-3 flex flex-wrap items-center gap-4 text-xs text-slate-600">
        <span className="inline-flex items-center gap-1.5"><span className="h-3 w-3 rounded-full" style={{ background: "#8FBE3D" }} /> Cumple</span>
        <span className="inline-flex items-center gap-1.5"><span className="h-3 w-3 rounded-full" style={{ background: "#cf4f2c" }} /> No cumple</span>
        <span className="inline-flex items-center gap-1.5"><span className="h-3 w-3 rounded-full" style={{ background: "#8a978f" }} /> Sin datos</span>
        {unplaced.length > 0 && (
          <span className="text-slate-500">
            · {unplaced.length} zona(s) sin ubicar{canEdit ? " (usa “Colocar zona”)" : ""}
          </span>
        )}
      </div>
    </div>
  );
}
