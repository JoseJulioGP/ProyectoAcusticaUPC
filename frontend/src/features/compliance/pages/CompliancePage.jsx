import { Card, PageHead, Field, Select, TextInput, Badge } from '@/ui/primitives';
import { useCumplimiento } from '../hooks/useCumplimiento';

const PERIODO_LABEL = { DIURNO: 'Diurno', NOCTURNO: 'Nocturno' };

export function CompliancePage() {
  const { filters, setFilters, zones, rows, loading, error } = useCumplimiento();
  const setF = (patch) => setFilters((f) => ({ ...f, ...patch }));
  // Solo se listan las zonas/periodos que cumplen la norma; los excesos y sus
  // acciones correctivas viven en el panel de Alertas.
  const okRows = rows.filter((r) => r.status === 'CUMPLE');
  // % de cumplimiento = evaluaciones que cumplen / total evaluadas en el rango.
  const rate = rows.length > 0 ? Math.round((okRows.length / rows.length) * 100) : 0;

  return (
    <div className="acu-stagger">
      <PageHead
        title="Cumplimiento"
        sub="Zonas y periodos que cumplen la Resolución 0627 de 2006 en el rango seleccionado. Los excesos y sus acciones correctivas están en el panel de Alertas."
      />

      <div className="mb-5 flex flex-wrap gap-3">
        <div className="rounded-xl border border-petroleo/10 bg-white px-4 py-2.5 min-w-[150px]">
          <p className="text-xs text-muted">Cumplimiento</p>
          <p className="text-xl font-semibold text-ink">{loading ? '—' : `${rate}%`}</p>
        </div>
      </div>

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
        <SectionTitle title="Zonas que cumplen la norma" loading={loading} />
        {okRows.length === 0 && !loading ? (
          <Empty>Ninguna zona cumple la norma en el rango seleccionado.</Empty>
        ) : (
          <div className="overflow-x-auto">
          <table className="w-full min-w-[480px] text-[13px] font-body">
            <thead>
              <tr className="text-left text-muted border-b border-petroleo/10">
                {filters.zoneId === 'todas' && <Th>Zona</Th>}
                <Th>Periodo</Th><Th>LAeq</Th><Th>L90</Th><Th>Estándar</Th><Th>Estado</Th>
              </tr>
            </thead>
            <tbody>
              {okRows.map((r) => (
                <tr key={r.id} className="border-b border-petroleo/5">
                  {filters.zoneId === 'todas' && <Td>{r.zoneName}</Td>}
                  <Td>{PERIODO_LABEL[r.period] ?? r.period}</Td>
                  <Td>{fmtDb(r.laeqDb)}</Td>
                  <Td>{fmtDb(r.l90Db)}</Td>
                  <Td>{fmtDb(r.standardDb)}</Td>
                  <Td>
                    <Badge kind={r.status}>Cumple</Badge>
                  </Td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
        )}
      </Card>
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
