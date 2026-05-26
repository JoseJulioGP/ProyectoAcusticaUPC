const STYLES = {
  LEVE:     "bg-yellow-100 text-yellow-800 border-yellow-300",
  MODERADA: "bg-orange-100 text-orange-800 border-orange-300",
  CRITICA:  "bg-red-100   text-red-800   border-red-300",
};

export default function AlertSeverityBadge({ severity }) {
  const cls = STYLES[severity] ?? "bg-gray-100 text-gray-800 border-gray-300";
  return (
    <span className={`inline-block px-2 py-0.5 rounded-md text-xs font-medium border ${cls}`}>
      {severity}
    </span>
  );
}