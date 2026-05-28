export default function KpiCard({ label, value, sub, accent = "slate", loading = false }) {
  const accents = {
    slate: "border-slate-200",
    green: "border-green-300",
    red: "border-red-300",
    amber: "border-amber-300",
    blue: "border-blue-300",
  };

  return (
    <div className={`bg-white rounded-lg border ${accents[accent]} p-4 shadow-sm`}>
      <p className="text-xs uppercase tracking-wide text-slate-500">{label}</p>
      {loading ? (
        <div className="h-8 w-24 bg-slate-200 animate-pulse mt-2 rounded"></div>
      ) : (
        <p className="text-3xl font-semibold text-slate-800 mt-1">{value}</p>
      )}
      {sub && <p className="text-xs text-slate-500 mt-2">{sub}</p>}
    </div>
  );
}