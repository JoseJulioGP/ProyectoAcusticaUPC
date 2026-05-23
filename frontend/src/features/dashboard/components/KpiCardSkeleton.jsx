export default function KpiCardSkeleton({ title, hint, icon: Icon, accent = 'indigo' }) {
  const accentMap = {
    indigo:  'bg-indigo-50 text-indigo-600',
    emerald: 'bg-emerald-50 text-emerald-600',
    amber:   'bg-amber-50 text-amber-600',
    sky:     'bg-sky-50 text-sky-600',
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
      <div className="flex items-start justify-between mb-3">
        <p className="text-sm text-slate-500">{title}</p>
        {Icon && (
          <div className={`p-2 rounded-lg ${accentMap[accent]}`}>
            <Icon size={18} />
          </div>
        )}
      </div>
      <div className="h-8 w-24 bg-slate-100 rounded animate-pulse mb-2" />
      <p className="text-xs text-slate-400">{hint}</p>
    </div>
  );
}