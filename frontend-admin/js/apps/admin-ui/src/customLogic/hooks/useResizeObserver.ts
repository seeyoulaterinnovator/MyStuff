import { useEffect, useRef } from "react";

interface TargetSize {
  width: number;
  height: number;
}

interface ParamsCallbackResizeObserver<T extends HTMLElement> {
  target: T | null;
  targetSize: TargetSize;
}

const useResizeObserver = <T extends HTMLElement = HTMLElement>(
  callback: (params: ParamsCallbackResizeObserver<T>) => void,
) => {
  const targetRef = useRef<T | null>(null);
  const observerRef = useRef<ResizeObserver | null>(null);

  useEffect(() => {
    if (targetRef.current) {
      observerRef.current = new ResizeObserver((entries) => {
        const entry = entries[0];
        const { width, height } = entry.contentRect;
        const targetSize: TargetSize = {
          width,
          height,
        };
        const params = { target: targetRef.current, targetSize };

        callback(params);
      });

      observerRef.current.observe(targetRef.current);
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [callback]);

  return { targetRef };
};

export default useResizeObserver;
