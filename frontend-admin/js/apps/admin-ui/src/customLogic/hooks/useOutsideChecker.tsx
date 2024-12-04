import { useEffect, useRef, useState } from "react";
import useResizeObserver from "./useResizeObserver";
import { isEqual } from "lodash-es";
import { mainPageContentId } from "@keycloak/keycloak-ui-shared";

type OutsideChecker = Partial<{
  top: boolean;
  right: boolean;
  bottom: boolean;
  left: boolean;
}>;

const initialOutsideChecker: OutsideChecker = {
  top: false,
  right: false,
  bottom: false,
  left: false,
};

interface ResizeObserverCallbackParams {
  checkedTargetRect?: DOMRect;
  containerRect?: DOMRect;
  targetAnchorRect?: DOMRect;
  recalculate: () => void;
}

interface UseOutsideCheckerProps {
  callback?: (params: ResizeObserverCallbackParams) => void;
}

export const useOutsideChecker = (props?: UseOutsideCheckerProps) => {
  const { callback } = props || {};

  const [isMayBeOutside, setIsMayBeOutside] = useState(initialOutsideChecker);
  const checkedTargetRef = useRef<HTMLElement | null>(null);
  const targetAnchorRef = useRef<HTMLElement | null>(null);

  const { targetRef } = useResizeObserver(({ target }) => {
    const checkedTargetRect = checkedTargetRef.current?.getBoundingClientRect();
    const targetAnchorRect = targetAnchorRef.current?.getBoundingClientRect();
    const containerRect = target?.getBoundingClientRect();

    const recalculate = () => {
      if (checkedTargetRect && containerRect) {
        let isMayBeOutsideRelativeAnchor: OutsideChecker = {};

        if (targetAnchorRect) {
          isMayBeOutsideRelativeAnchor = {
            top:
              targetAnchorRect.bottom - checkedTargetRect.height <
              containerRect.top,
            right:
              targetAnchorRect.left + checkedTargetRect.width >
              containerRect.right,
            bottom:
              targetAnchorRect.top + checkedTargetRect.height >
              containerRect.top,
            left:
              targetAnchorRect.right - checkedTargetRect.width <
              containerRect.left,
          };
        }

        setIsMayBeOutside((prevIsMayBeOutside) => {
          const newIsOutside: OutsideChecker = {
            top:
              checkedTargetRect.top < containerRect.top ||
              isMayBeOutsideRelativeAnchor.top,
            right:
              checkedTargetRect.right > containerRect.right ||
              isMayBeOutsideRelativeAnchor.right,
            bottom:
              checkedTargetRect.bottom > containerRect.bottom ||
              isMayBeOutsideRelativeAnchor.bottom,
            left:
              checkedTargetRect.left < containerRect.left ||
              isMayBeOutsideRelativeAnchor.left,
          };

          if (isEqual(prevIsMayBeOutside, newIsOutside)) {
            return prevIsMayBeOutside;
          }

          return newIsOutside;
        });
      } else {
        setIsMayBeOutside(initialOutsideChecker);
      }
    };

    if (callback) {
      callback({
        checkedTargetRect,
        containerRect,
        targetAnchorRect,
        recalculate,
      });
    } else {
      recalculate();
    }
  });

  useEffect(() => {
    targetRef.current = document.getElementById(mainPageContentId);
  }, []);

  return {
    isMayBeOutside,
    checkedTargetRef,
    targetAnchorRef,
    containerRef: targetRef,
  };
};
