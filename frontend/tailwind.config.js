
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        petroleo: { DEFAULT: '#13694A', deep: '#0f5840', deeper: '#0a3327' },
        rombo: { verde: '#8FBE3D', azul: '#2E93C9', cafe: '#7A4E22', naranja: '#F0A02A' },
        paper: '#F4F1EA', ink: '#15241d', ink2: '#41524a', muted: '#8a978f',
        danger: '#cf4f2c', dangerDeep: '#b23d1e', warn: '#C98A12',
      },
      fontFamily: { display: ['Sora', 'sans-serif'], body: ['Manrope', 'sans-serif'] },
      borderRadius: { xl2: '22px' },
    },
  },
  plugins: [],
};