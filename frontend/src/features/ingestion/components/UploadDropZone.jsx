import { useState, useCallback, useEffect } from "react";
import { useDropzone } from "react-dropzone";
import { Upload, FileSpreadsheet, X } from "lucide-react";
import { ingestionApi } from "../api/ingestionApi";
import { zonesApi } from "../../zones/api/zonesApi";

export default function UploadDropZone({ onUploadSuccess }) {
  const [zones, setZones] = useState([]);
  const [selectedZoneId, setSelectedZoneId] = useState("");
  const [file, setFile] = useState(null);
  const [observation, setObservation] = useState("");
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    zonesApi.list()
      .then(setZones)
      .catch(() => setError("No se pudieron cargar las zonas"));
  }, []);

  const onDrop = useCallback((acceptedFiles) => {
    if (acceptedFiles.length > 0) {
      setFile(acceptedFiles[0]);
      setError(null);
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": [".xlsx"],
      "application/vnd.ms-excel": [".xls"],
    },
    maxFiles: 1,
    maxSize: 10 * 1024 * 1024, // 10 MB
  });

  const handleSubmit = async () => {
    if (!file || !selectedZoneId) {
      setError("Selecciona zona y archivo");
      return;
    }
    setUploading(true);
    setError(null);
    try {
      const result = await ingestionApi.upload(file, selectedZoneId);
      const obs = observation.trim();
      if (obs) {
        try {
          await ingestionApi.updateObservation(result.batchId, obs);
        } catch {
          setError("La carga se inició, pero no se pudo guardar la observación.");
        }
      }
      onUploadSuccess?.(result);
      setFile(null);
      setSelectedZoneId("");
      setObservation("");
    } catch (err) {
      setError(err.response?.data?.message ?? "Error al subir el archivo");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="text-lg font-semibold text-slate-800 mb-4">Nueva carga</h2>

      {/* Selector de zona */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-slate-700 mb-1">Zona</label>
        <select
          value={selectedZoneId}
          onChange={(e) => setSelectedZoneId(e.target.value)}
          disabled={uploading}
          className="w-full rounded-md border-slate-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        >
          <option value="">— Selecciona una zona —</option>
          {zones.map((z) => (
            <option key={z.id} value={z.id}>{z.name}</option>
          ))}
        </select>
      </div>

      {/* Dropzone */}
      <div
        {...getRootProps()}
        className={`mt-2 flex justify-center rounded-lg border-2 border-dashed px-6 py-10 cursor-pointer transition-colors ${
          isDragActive ? "border-indigo-500 bg-indigo-50" : "border-slate-300 hover:border-slate-400"
        }`}
      >
        <input {...getInputProps()} />
        <div className="text-center">
          {file ? (
            <div className="flex items-center justify-center gap-2 text-slate-700">
              <FileSpreadsheet className="h-6 w-6 text-emerald-600" />
              <span className="font-medium">{file.name}</span>
              <button
                type="button"
                onClick={(e) => { e.stopPropagation(); setFile(null); }}
                className="ml-2 rounded p-1 hover:bg-slate-200"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          ) : (
            <>
              <Upload className="mx-auto h-10 w-10 text-slate-400" />
              <p className="mt-2 text-sm text-slate-600">
                {isDragActive ? "Suelta el archivo aquí…" : "Arrastra un .xls o .xlsx, o haz click para seleccionar"}
              </p>
              <p className="mt-1 text-xs text-slate-500">Máximo 10 MB</p>
            </>
          )}
        </div>
      </div>

      {/* Observación: inconveniente o factor que pudo afectar el ruido */}
      <div className="mt-4">
        <label className="block text-sm font-medium text-slate-700 mb-1">
          Observación <span className="font-normal text-slate-400">(opcional)</span>
        </label>
        <textarea
          value={observation}
          onChange={(e) => setObservation(e.target.value)}
          disabled={uploading}
          rows={3}
          maxLength={4000}
          placeholder="¿Algún inconveniente o factor que pudo afectar el ruido durante la medición? (obra cercana, evento, tráfico inusual, falla del equipo…)"
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>

      {error && (
        <div className="mt-3 rounded-md bg-rose-50 px-3 py-2 text-sm text-rose-700 ring-1 ring-rose-200">
          {error}
        </div>
      )}

      <button
        onClick={handleSubmit}
        disabled={!file || !selectedZoneId || uploading}
        className="mt-4 w-full rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 disabled:bg-slate-300 disabled:cursor-not-allowed"
      >
        {uploading ? "Subiendo…" : "Iniciar ingesta"}
      </button>
    </div>
  );
}