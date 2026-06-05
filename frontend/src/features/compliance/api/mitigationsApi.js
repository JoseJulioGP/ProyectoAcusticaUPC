import apiClient from "../../../shared/api/apiClient";

export const mitigationsApi = {
  // Catálogo de acciones activas.
  list() {
    return apiClient.get("/mitigations").then((r) => r.data);
  },
  // Sugerencias por exceso de dB; el backend las devuelve ordenadas por priority asc.
  suggest(excessDb) {
    return apiClient
      .get("/mitigations/suggest", { params: { excessDb } })
      .then((r) => r.data);
  },
};
