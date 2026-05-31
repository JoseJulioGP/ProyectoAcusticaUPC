import { useState, useEffect } from 'react';

export function useIsMobile(bp = 900) {
  const [m, setM] = useState(typeof window !== 'undefined' && window.innerWidth < bp);
  useEffect(() => {
    const r = () => setM(window.innerWidth < bp);
    window.addEventListener('resize', r);
    return () => window.removeEventListener('resize', r);
  }, [bp]);
  return m;
}