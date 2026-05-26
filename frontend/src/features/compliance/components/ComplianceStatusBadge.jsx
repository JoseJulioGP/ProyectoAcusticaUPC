export default function ComplianceStatusBadge({ status }) {
  const ok = status === "CUMPLE";
  return (
    <span className={`inline-block px-2 py-0.5 rounded-md text-xs font-semibold border
      ${ok ? "bg-emerald-100 text-emerald-800 border-emerald-300"
           : "bg-red-100 text-red-800 border-red-300"}`}>
      {ok ? "CUMPLE" : "NO CUMPLE"}
    </span>
  );
}