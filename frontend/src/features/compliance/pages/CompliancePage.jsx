import React from 'react';
import { Card, PageHead, Field, Select, TextInput, Badge } from '@/ui/primitives';
import { useCumplimiento } from '../hooks/useCumplimiento';

const PERIODO_LABEL = { DIURNO: 'Diurno', NOCTURNO: 'Nocturno' };

export function CompliancePage() {
  const { filters, setFilters, zones, rows, alerts, loading, error } = useCumplimiento();
  const setF = (patch) => setFilters((f) => ({ ...f, ...patch }));

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

      <div className="grid gap-5 lg:grid-cols-2">
        <Card>
          <SectionTitle title="Resultados por zona / periodo" loading={loading} />
          {filters.zoneId === 'todas' ? (
            <Empty>Selecciona una zona específica para ver sus resultados por periodo.</Empty>
          ) : rows.length === 0 && !loading ? (
            <Empty>Sin resultados en el rango.</Empty>
          ) : (
            <table className="w-full text-[13px] font-body">
              <thead>
                <tr className="text-left text-muted border-b border-petroleo/10">
                  <Th>Periodo</Th><Th>LAeq</Th><Th>L90</Th><Th>Estándar</Th><Th>Estado</Th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id} className="border-b border-petroleo/5">
                    <Td>{PERIODO_LABEL[r.period] ?? r.period}</Td>
                    <Td>{fmtDb(r.laeqDb)}</Td>
                    <Td>{fmtDb(r.l90Db)}</Td>
                    <Td>{fmtDb(r.standardDb)}</Td>
                    <Td>
                      <Badge kind={r.status}>{r.status === 'CUMPLE' ? 'Cumple' : 'Excede'}</Badge>
                    </Td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>

        <Card>
          <SectionTitle title="Alertas en el rango" loading={loading} />
          {alerts.length === 0 && !loading ? (
            <Empty>Sin alertas en el rango seleccionado.</Empty>
          ) : (
            <table className="w-full text-[13px] font-body">
              <thead>
                <tr className="text-left text-muted border-b border-petroleo/10">
                  <Th>Zona</Th><Th>Periodo</Th><Th>Medido</Th><Th>Exceso</Th><Th>Severidad</Th>
                </tr>
              </thead>
              <tbody>
                {alerts.map((a) => (
                  <tr key={a.id} className="border-b border-petroleo/5">
                    <Td>{a.zoneName}</Td>
                    <Td>{PERIODO_LABEL[a.period] ?? a.period}</Td>
                    <Td>{fmtDb(a.measuredDb)}</Td>
                    <Td>+{fmtDb(a.excessDb)}</Td>
                    <Td>
                      <Badge kind={a.severity} alert={a.severity === 'CRITICA'}>{a.severity}</Badge>
                    </Td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      </div>
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
