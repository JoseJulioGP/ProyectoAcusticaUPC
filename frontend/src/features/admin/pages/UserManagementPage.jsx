import { useState } from "react";
import { useUsers } from "../hooks/useUsers";
import { usersApi } from "../api/usersApi";
import UserTable from "../components/UserTable";
import UserFormModal from "../components/UserFormModal";
import ResetPasswordModal from "../components/ResetPasswordModal";

export default function UserManagementPage() {
  const { users, loading, error, reload } = useUsers();
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [pwdUser, setPwdUser] = useState(null);

  const openCreate = () => { setEditing(null); setFormOpen(true); };
  const openEdit = (u) => { setEditing(u); setFormOpen(true); };

  const handleSubmit = async (payload) => {
    if (editing) await usersApi.update(editing.id, payload);
    else await usersApi.create(payload);
    await reload();
  };

  const toggleActive = async (u) => {
    if (u.active) await usersApi.deactivate(u.id);
    else await usersApi.update(u.id, { fullName: u.fullName, role: u.role, active: true });
    await reload();
  };

  const resetPwd = async (newPassword) => {
    await usersApi.resetPassword(pwdUser.id, newPassword);
  };

  return (
    <div>
      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-xl sm:text-2xl font-semibold text-gray-900">Gestión de usuarios</h1>
        <button onClick={openCreate} className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 self-start sm:self-auto">
          + Nuevo usuario
        </button>
      </div>

      {loading && <p className="text-gray-500">Cargando…</p>}
      {error && <p className="rounded bg-red-50 px-3 py-2 text-red-700">{error}</p>}
      {!loading && !error && (
        <UserTable
          users={users}
          onEdit={openEdit}
          onResetPassword={(u) => setPwdUser(u)}
          onToggleActive={toggleActive}
        />
      )}

      <UserFormModal open={formOpen} initial={editing}
                     onClose={() => setFormOpen(false)} onSubmit={handleSubmit} />
      <ResetPasswordModal open={Boolean(pwdUser)} user={pwdUser}
                          onClose={() => setPwdUser(null)} onSubmit={resetPwd} />
    </div>
  );
}
