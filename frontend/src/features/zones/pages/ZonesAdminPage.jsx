import { useEffect, useState } from "react";
import { Plus } from "lucide-react";
import { useZones } from "../hooks/useZones";
import { zonesApi } from "../api/zonesApi";
import { analyticsApi } from "../../analytics/api/analyticsApi";
import { useRole } from "../../auth/hooks/useRole";
import { useToast } from "../../../shared/ui/useToast";
import ZonesTable from "../components/ZonesTable";
import ZoneFormModal from "../components/ZoneFormModal";
import ZoneStatsTiles from "../components/ZoneStatsTiles";
import Modal from "../../../shared/ui/Modal";
import { Button } from "@/ui/primitives";

export default function ZonesAdminPage() {
  const { zones, loading, error, reload } = useZones();
  const { isAdmin, isAnalyst, isViewer } = useRole();
  const canManage = isAdmin || isAnalyst;
  // /analytics/zones/stats es ADMIN/VIEWER (no ANALYST) → solo lo pedimos para esos.
  const canViewStats = isAdmin || isViewer;
  const toast = useToast();

  // Estado por zona (últimos 30 días), para las tarjetas de detalle.
  const [range] = useState(() => {
    const to = new Date();
    const from = new Date(to.getTime() - 30 * 24 * 60 * 60 * 1000);
    return { from: from.toISOString(), to: to.toISOString() };
  });
  const [stats, setStats] = useState([]);
  useEffect(() => {
    if (!canViewStats) return;
    analyticsApi
      .zonesStats({ from: range.from, to: range.to })
      .then((d) => setStats(Array.isArray(d) ? d : []))
      .catch(() => setStats([]));
  }, [canViewStats, range]);

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [deleteBusy, setDeleteBusy] = useState(false);

  const openCreate = () => { setEditing(null); setFormOpen(true); };
  const openEdit = (z) => { setEditing(z); setFormOpen(true); };

  const handleSubmit = async (payload) => {
    if (editing) {
      await zonesApi.update(editing.id, payload);
      toast.success("Zona actualizada.");
    } else {
      await zonesApi.create(payload);
      toast.success("Zona creada.");
    }
    await reload();
  };

  const doDelete = async (hard) => {
    setDeleteBusy(true);
    try {
      await zonesApi.remove(deleting.id, hard);
      toast.success(hard ? "Zona eliminada permanentemente." : "Zona desactivada.");
      setDeleting(null);
      await reload();
    } catch (err) {
      const code = err.response?.data?.message?.split(":")[0];
      if (code === "ZONE_IN_USE") {
        toast.error("No se puede borrar: tiene mediciones. Desactívala en su lugar.");
      } else {
        toast.error(err.response?.data?.message || "No se pudo eliminar la zona.");
      }
    } finally {
      setDeleteBusy(false);
    }
  };

  return (
    <div className="acu-stagger space-y-5">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Zonas</h1>
          <p className="mt-1 text-sm text-slate-600">
            Administra las zonas de monitoreo acústico de la UPC.
          </p>
        </div>
        {canManage && (
          <Button onClick={openCreate}><Plus size={17} /> Nueva zona</Button>
        )}
      </div>

      {loading && <p className="text-sm text-slate-500">Cargando zonas…</p>}
      {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}
      {!loading && !error && (
        <ZonesTable
          zones={zones}
          canManage={canManage}
          onEdit={openEdit}
          onDelete={(z) => setDeleting(z)}
        />
      )}

      {canViewStats && <ZoneStatsTiles stats={stats} range={range} />}

      <ZoneFormModal
        open={formOpen}
        initial={editing}
        onClose={() => setFormOpen(false)}
        onSubmit={handleSubmit}
      />

      {/* Modal de borrado: soft (desactivar) vs hard (permanente) con manejo 409 */}
      <Modal
        open={!!deleting}
        onClose={() => !deleteBusy && setDeleting(null)}
        title={`Eliminar zona "${deleting?.name ?? ""}"`}
        footer={
          <>
            <Button variant="ghost" onClick={() => !deleteBusy && setDeleting(null)}>Cancelar</Button>
            <Button onClick={() => doDelete(false)} disabled={deleteBusy}>
              {deleteBusy ? "Procesando…" : "Desactivar"}
            </Button>
            <button
              type="button"
              onClick={() => doDelete(true)}
              disabled={deleteBusy}
              className="rounded-[11px] bg-rose-600 px-[18px] py-[11px] font-display font-semibold text-[14.5px] text-white hover:bg-rose-500 disabled:opacity-50"
            >
              Eliminar permanentemente
            </button>
          </>
        }
      >
        <p className="text-sm text-slate-600">
          <strong>Desactivar</strong> oculta la zona pero conserva sus mediciones (soft delete).{" "}
          <strong>Eliminar permanentemente</strong> intenta borrarla; si tiene mediciones asociadas
          el sistema lo impedirá y deberás desactivarla.
        </p>
      </Modal>
    </div>
  );
}
