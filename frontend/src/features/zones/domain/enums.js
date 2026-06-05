// Enums de sector/subsector (Resolución 0627 de 2006), clave -> etiqueta visible.
// Las CLAVES coinciden 1:1 con los enums del backend
// (com.upc.acusticupc.zones.domain.model.Sector / Subsector).

export const SECTORS = {
  A_TRANQUILIDAD_SILENCIO: "Sector A — Tranquilidad y Silencio",
  B_TRANQUILIDAD_RUIDO_MODERADO: "Sector B — Tranquilidad y Ruido Moderado",
  C_RUIDO_INTERMEDIO_RESTRINGIDO: "Sector C — Ruido Intermedio Restringido",
  D_SUBURBANA_RURAL: "Sector D — Suburbana o Rural",
};

export const SUBSECTORS = {
  // Sector A
  BIBLIOTECAS_HOSPITALES: "Bibliotecas / Hospitales / Guarderías",
  // Sector B
  RESIDENCIAL_HOTELERIA: "Residencial / Hotelería",
  UNIVERSIDADES_COLEGIOS: "Universidades / Colegios / Centros de estudio",
  PARQUES_URBANOS: "Parques urbanos",
  // Sector C
  INDUSTRIAL: "Industrial",
  COMERCIAL: "Comercial / Centros deportivos / Restaurantes / Bares",
  OFICINAS_INSTITUCIONAL: "Oficinas / Institucional",
  OTROS_USOS_AIRE_LIBRE: "Parques mecánicos / Espectáculos al aire libre",
  // Sector D
  RESIDENCIAL_SUBURBANA: "Residencial suburbana",
  RURAL_AGROPECUARIA: "Rural / Agropecuaria",
  RECREACION_NATURAL: "Recreación / Parques naturales",
};

// Subsectores válidos por sector (mismo agrupamiento que el enum del backend y
// que los estándares sembrados en noise_standards). Un combo fuera de esto no
// tiene estándar → rompe la evaluación de cumplimiento y la analítica.
export const SUBSECTORS_BY_SECTOR = {
  A_TRANQUILIDAD_SILENCIO: ["BIBLIOTECAS_HOSPITALES"],
  B_TRANQUILIDAD_RUIDO_MODERADO: ["RESIDENCIAL_HOTELERIA", "UNIVERSIDADES_COLEGIOS", "PARQUES_URBANOS"],
  C_RUIDO_INTERMEDIO_RESTRINGIDO: ["INDUSTRIAL", "COMERCIAL", "OFICINAS_INSTITUCIONAL", "OTROS_USOS_AIRE_LIBRE"],
  D_SUBURBANA_RURAL: ["RESIDENCIAL_SUBURBANA", "RURAL_AGROPECUARIA", "RECREACION_NATURAL"],
};

export const sectorLabel = (key) => SECTORS[key] ?? key ?? "—";
export const subsectorLabel = (key) => SUBSECTORS[key] ?? key ?? "—";

export const sectorOptions = Object.entries(SECTORS).map(([value, label]) => ({ value, label }));
export const subsectorOptions = Object.entries(SUBSECTORS).map(([value, label]) => ({ value, label }));

// Opciones de subsector válidas para el sector dado.
export const subsectorOptionsForSector = (sector) =>
  (SUBSECTORS_BY_SECTOR[sector] ?? []).map((value) => ({ value, label: SUBSECTORS[value] }));
