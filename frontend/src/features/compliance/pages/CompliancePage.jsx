import React, { useState } from 'react';
import { Card, PageHead, Field, Select, TextInput, Badge } from '@/ui/primitives';
import { useCumplimiento } from '../hooks/useCumplimiento';
import Modal from '../../../shared/ui/Modal';
import MitigationActions from '../components/MitigationActions';
import BatchObservationNote from '../../ingestion/components/BatchObservationNote';

const PERIODO_LABEL = { DIURNO: 'Diurno', NOCTURNO: 'Nocturno' };

export function CompliancePage() {
  const { filters, setFilters, zones, rows, loading, error } = useCumplimiento();
  const setF = (patch) => setFilters((f) => ({ ...f, ...patch }));
  const [actionsFor, setActionsFor] = useState(null); // fila que excede, para ver acciones

  return (
    <div className="acu-stagger">
      <PageHead
        title="Cumplimiento"
        sub="Resultados de evaluación frente a la Resolución 0627 de 2006 y alertas activas en el rango seleccionado."
      />

      <Card className="mb-5">
        <div className="grid gap-4 md:grid-cols-3">
          <Field label="Zona">
            <Select
              value={filters.zoneId}
              onChange={(v) => setF({ zoneId: v })}
              options={[
                { value: 'todas', label: 'Todas las zonas' },
                ...zones.map((z) => ({ value: z.id, label: z.name })),
              ]}
            />
          </Field>
          <Field label="Desde">
            <TextInput
              type="date"
              value={filters.from}
              max={filters.to}
              onChange={(e) => setF({ from: e.target.value })}
            />
          </Field>
          <Field label="Hasta">
            <TextInput
              type="date"
              value={filters.to}
              min={filters.from}
              onChange={(e) => setF({ to: e.target.value })}
            />
          </Field>
        </div>
      </Card>

      {error && (
        <Card className="mb-5" accent="#cf4f2c">
          <div className="font-body text-[14px] text-dangerDeep">{error}</div>
        </Card>
      )}

      <Card>
        <SectionTitle title="Resultados por zona / periodo" loading={loading} />
        {rows.length === 0 && !loading ? (
          <Empty>Sin resultados en el rango.</Empty>
        ) : (
          <div className="overflow-x-auto">
          <table className="w-full min-w-[520px] text-[13px] font-body">
            <thead>
              <tr className="text-left text-muted border-b border-petroleo/10">
                {filters.zoneId === 'todas' && <Th>Zona</Th>}
                <Th>Periodo</Th><Th>LAeq</Th><Th>L90</Th><Th>Estándar</Th><Th>Estado</Th><Th>Acciones</Th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.id} className="border-b border-petroleo/5">
                  {filters.zoneId === 'todas' && <Td>{r.zoneName}</Td>}
                  <Td>{PERIODO_LABEL[r.period] ?? r.period}</Td>
                  <Td>{fmtDb(r.laeqDb)}</Td>
                  <Td>{fmtDb(r.l90Db)}</Td>
                  <Td>{fmtDb(r.standardDb)}</Td>
                  <Td>
                    <Badge kind={r.status}>{r.status === 'CUMPLE' ? 'Cumple' : 'Excede'}</Badge>
                  </Td>
                  <Td>
                    {r.status !== 'CUMPLE' ? (
                      <button
                        type="button"
                        onClick={() => setActionsFor(r)}
                        className="font-semibold text-petroleo hover:underline"
                      >
                        Ver acciones
                      </button>
                    ) : (
                      <span className="text-muted">—</span>
                    )}
                  </Td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
        )}
      </Card>

      {/* Acciones correctivas / mitigación para una zona que excede */}
      <Modal
        open={!!actionsFor}
        onClose={() => setActionsFor(null)}
        title="Acciones correctivas recomendadas"
        width={560}
        footer={
          <button
            type="button"
            onClick={() => setActionsFor(null)}
            className="rounded-[11px] border-[1.5px] border-petroleo/15 px-4 py-2 text-sm font-semibold text-petroleo hover:bg-petroleo/[0.06]"
          >
            Cerrar
          </button>
        }
      >
        {actionsFor && (
          <div className="space-y-4">
            <p className="text-sm text-slate-600">
              <strong>{actionsFor.zoneName}</strong> · {PERIODO_LABEL[actionsFor.period] ?? actionsFor.period}
              {" · "}Exceso <span className="font-semibold text-rose-700">+{fmtDb(actionsFor.excessDb)}</span>
            </p>
            <div>
              <h3 className="font-semibold text-slate-800 mb-1.5">Observación de la carga</h3>
              <BatchObservationNote batchId={actionsFor.batchId} />
            </div>
            <div>
              <h3 className="font-semibold text-slate-800 mb-1.5">Acciones recomendadas</h3>
              <MitigationActions excessDb={actionsFor.excessDb} />
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

function SectionTitle({ title, loading }) {
  return (
    <div className="flex items-center justify-between mb-3">
      <h2 className="font-display font-semibold text-[17px] text-ink m-0">{title}</h2>
      {loading && <span className="font-body text-[12px] text-muted">cargando…</span>}
    </div>
  );
}

function Th({ children }) {
  return <th className="font-semibold text-[12px] tracking-[0.02em] uppercase py-2 pr-3">{children}</th>;
}
function Td({ children }) {
  return <td className="py-2 pr-3 text-ink">{children}</td>;
}
function Empty({ children }) {
  return <div className="font-body text-[13px] text-muted py-6 text-center">{children}</div>;
}
function fmtDb(v) {
  return v == null ? '—' : `${Number(v).toFixed(1)} dB`;
}
