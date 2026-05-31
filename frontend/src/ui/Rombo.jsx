export function Rombo({ size = 30, gap = '#0f5840' }) {
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" aria-label="AcústicaUPC">
      <polygon points="50,2 2,50 50,50" fill="#F0A02A" />
      <polygon points="50,2 98,50 50,50" fill="#8FBE3D" />
      <polygon points="98,50 50,98 50,50" fill="#2E93C9" />
      <polygon points="2,50 50,98 50,50" fill="#7A4E22" />
      <g stroke={gap} strokeWidth="2.4" strokeLinecap="round">
        <line x1="50" y1="2" x2="50" y2="98" /><line x1="2" y1="50" x2="98" y2="50" />
      </g>
    </svg>
  );
}