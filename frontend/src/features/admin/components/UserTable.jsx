const ROLE_LABEL = { ADMIN: "Administrador", ANALYST: "Analista", VIEWER: "Observador" };

export default function UserTable({ users, onEdit, onResetPassword, onToggleActive }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200">
      <table className="min-w-full text-sm">
        <thead className="bg-gray-50 text-left text-gray-600">
          <tr>
            <th className="px-4 py-3">Nombre</th>
            <th className="px-4 py-3">Email</th>
            <th className="px-4 py-3">Rol</th>
            <th className="px-4 py-3">Estado</th>
            <th className="px-4 py-3 text-right">Acciones</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {users.map((u) => (
            <tr key={u.id} className={u.active ? "" : "opacity-50"}>
              <td className="px-4 py-3 font-medium text-gray-900">{u.fullName}</td>
              <td className="px-4 py-3 text-gray-600">{u.email}</td>
              <td className="px-4 py-3">{ROLE_LABEL[u.role] ?? u.role}</td>
              <td className="px-4 py-3">
                <span className={`rounded-full px-2 py-1 text-xs ${
                  u.active ? "bg-green-100 text-green-700" : "bg-gray-200 text-gray-600"
                }`}>
                  {u.active ? "Activo" : "Inactivo"}
                </span>
              </td>
              <td className="px-4 py-3 text-right space-x-2 whitespace-nowrap">
                <button onClick={() => onEdit(u)} className="text-blue-600 hover:underline">Editar</button>
                <button onClick={() => onResetPassword(u)} className="text-amber-600 hover:underline">Clave</button>
                <button onClick={() => onToggleActive(u)} className="text-red-600 hover:underline">
                  {u.active ? "Desactivar" : "Activar"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
