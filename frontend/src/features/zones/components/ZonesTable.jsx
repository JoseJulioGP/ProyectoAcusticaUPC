import { sectorLabel, subsectorLabel } from "../domain/enums";

export default function ZonesTable({ zones, canManage, onEdit, onDelete }) {
  if (!zones || zones.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
        No hay zonas registradas.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            <Th>Código</Th>
            <Th>Nombre</Th>
            <Th>Sector</Th>
            <Th>Subsector</Th>
            <Th>Edificio</Th>
            <Th>Piso</Th>
            <Th>Estado</Th>
            {canManage && <th className="px-4 py-3 text-right text-xs font-semibold text-slate-600 uppercase tracking-wider">Acciones</th>}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {zones.map((z) => (
            <tr key={z.id} className={`acu-trow ${z.active === false ? "opacity-50" : ""}`}>
              <td className="px-4 py-3 text-slate-500 tabular-nums">{z.code ?? "—"}</td>
              <td className="px-4 py-3 font-medium text-slate-900">{z.name}</td>
              <td className="px-4 py-3 text-slate-600">{sectorLabel(z.sector)}</td>
              <td className="px-4 py-3 text-slate-600">{subsectorLabel(z.subsector)}</td>
              <td className="px-4 py-3 text-slate-600">{z.building ?? "—"}</td>
              <td className="px-4 py-3 text-slate-600">{z.floor ?? "—"}</td>
              <td className="px-4 py-3">
                <span className={`rounded-full px-2 py-1 text-xs font-semibold ${
                  z.active === false ? "bg-slate-200 text-slate-600" : "bg-emerald-100 text-emerald-700"
                }`}>
                  {z.active === false ? "Inactiva" : "Activa"}
                </span>
              </td>
              {canManage && (
                <td className="px-4 py-3 text-right whitespace-nowrap space-x-3">
                  <button onClick={() => onEdit(z)} className="text-xs font-medium text-petroleo hover:underline">Editar</button>
                  <button onClick={() => onDelete(z)} className="text-xs font-medium text-rose-600 hover:underline">Eliminar</button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function Th({ children }) {
  return (
    <th className="px-4 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
      {children}
    </th>
  );
}
