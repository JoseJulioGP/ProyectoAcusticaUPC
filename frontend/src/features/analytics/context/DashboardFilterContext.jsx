import { createContext, useContext, useMemo, useState } from "react";

const DashboardFilterContext = createContext(null);

// Helpers para presets
const isoNow = () => new Date().toISOString();
const isoDaysAgo = (n) => {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString();
};

export function DashboardFilterProvider({ children }) {
  const [from, setFrom] = useState(isoDaysAgo(30));
  const [to, setTo] = useState(isoNow());
  const [zoneId, setZoneId] = useState(null); // null = todas
  const [period, setPeriod] = useState(null); // null = ambos

  const setPreset = (preset) => {
    switch (preset) {
      case "7d":
        setFrom(isoDaysAgo(7));
        setTo(isoNow());
        break;
      case "30d":
        setFrom(isoDaysAgo(30));
        setTo(isoNow());
        break;
      case "1y":
        setFrom(isoDaysAgo(365));
        setTo(isoNow());
        break;
      default:
    }
  };

  const value = useMemo(
    () => ({ from, to, zoneId, period, setFrom, setTo, setZoneId, setPeriod, setPreset }),
    [from, to, zoneId, period]
  );

  return (
    <DashboardFilterContext.Provider value={value}>
      {children}
    </DashboardFilterContext.Provider>
  );
}

export function useDashboardFilter() {
  const ctx = useContext(DashboardFilterContext);
  if (!ctx) {
    throw new Error("useDashboardFilter debe usarse dentro de DashboardFilterProvider");
  }
  return ctx;
}