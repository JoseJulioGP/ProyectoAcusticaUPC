import { useEffect, useState } from "react";
import Modal from "../../../shared/ui/Modal";
import { Button, Field, TextInput, Select } from "@/ui/primitives";
import { sectorOptions, subsectorOptionsForSector, SUBSECTORS_BY_SECTOR } from "../domain/enums";

const EMPTY = {
  name: "",
  description: "",
  building: "",
  floor: "",
  sector: "",
  subsector: "",
  active: true,
};

const SECTOR_OPTS = [{ value: "", label: "— Selecciona sector —" }, ...sectorOptions];

export default function ZoneFormModal({ open, initial, onClose, onSubmit }) {
  const isEdit = Boolean(initial?.id);
  const [form, setForm] = useState(EMPTY);
  const [err, setErr] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) {
      setForm({
        name: initial?.name ?? "",
        description: initial?.description ?? "",
        building: initial?.building ?? "",
        floor: initial?.floor ?? "",
        sector: initial?.sector ?? "",
        subsector: initial?.subsector ?? "",
        active: initial?.active ?? true,
      });
      setErr("");
    }
  }, [open, initial]);

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  // Al cambiar el sector, limpia el subsector si ya no pertenece a ese sector.
  const setSector = (v) =>
    setForm((f) => {
      const valid = SUBSECTORS_BY_SECTOR[v] ?? [];
      return { ...f, sector: v, subsector: valid.includes(f.subsector) ? f.subsector : "" };
    });

  const subsectorOpts = [
    { value: "", label: form.sector ? "— Selecciona subsector —" : "— Elige primero un sector —" },
    ...subsectorOptionsForSector(form.sector),
  ];

  const submit = async (e) => {
    e?.preventDefault();
    if (!form.name.trim()) { setErr("El nombre es obligatorio."); return; }
    if (!form.sector) { setErr("El sector es obligatorio."); return; }
    if (!form.subsector) { setErr("El subsector es obligatorio."); return; }
    setErr("");
    setSaving(true);
    try {
      // mapX/mapY/mapWidth/mapHeight quedan fuera (Sprint 8 con react-leaflet).
      await onSubmit({
        name: form.name.trim(),
        description: form.description.trim() || null,
        building: form.building.trim() || null,
        floor: form.floor.trim() || null,
        sector: form.sector,
        subsector: form.subsector,
        active: form.active,
      });
      onClose();
    } catch (e2) {
      setErr(e2?.response?.data?.message ?? "No se pudo guardar la zona.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={() => !saving && onClose()}
      title={isEdit ? "Editar zona" : "Nueva zona"}
      width={520}
      footer={
        <>
          <Button variant="ghost" onClick={() => !saving && onClose()}>Cancelar</Button>
          <Button onClick={submit} disabled={saving}>{saving ? "Guardando…" : "Guardar"}</Button>
        </>
      }
    >
      <form onSubmit={submit} className="flex flex-col gap-4">
        {err && (
          <div className="rounded-lg bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700 ring-1 ring-rose-200">
            {err}
          </div>
        )}

        <Field label="Nombre *">
          <TextInput value={form.name} autoFocus placeholder="Nombre de la zona"
            onChange={(e) => set("name", e.target.value)} />
        </Field>

        <Field label="Descripción">
          <TextInput value={form.description} placeholder="Opcional"
            onChange={(e) => set("description", e.target.value)} />
        </Field>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Edificio">
            <TextInput value={form.building} placeholder="Opcional"
              onChange={(e) => set("building", e.target.value)} />
          </Field>
          <Field label="Piso">
            <TextInput value={form.floor} placeholder="Opcional"
              onChange={(e) => set("floor", e.target.value)} />
          </Field>
        </div>

        <Field label="Sector *">
          <Select value={form.sector} onChange={setSector} options={SECTOR_OPTS} />
        </Field>

        <Field label="Subsector *">
          <Select value={form.subsector} onChange={(v) => set("subsector", v)} options={subsectorOpts} />
        </Field>

        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" checked={form.active}
            onChange={(e) => set("active", e.target.checked)} />
          Zona activa
        </label>
      </form>
    </Modal>
  );
}
