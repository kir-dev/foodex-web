'use client';

import { usePathname } from 'next/navigation';
import { useEffect, useRef } from 'react';

/** Re-run `load` on mount and whenever this route is shown again. */
export function useRefetchOnPath(load: () => void | Promise<void>): void {
  const pathname = usePathname();
  const loadRef = useRef(load);
  loadRef.current = load;

  useEffect(() => {
    void loadRef.current();
  }, [pathname]);
}
