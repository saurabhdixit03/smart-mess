import {
  useEffect,
  useId,
  useRef,
  useState,
} from "react";

import {
  ChevronLeft,
  ChevronRight,
} from "lucide-react";

import MealRecordCard from "./MealRecordCard";

import type {
  CollectionQueueItem,
  MealCollectionSelection,
} from "../types";

type MealRecordQueueProps = {
  items: CollectionQueueItem[];
  onRecord: (item: MealCollectionSelection) => void;
};

type ScrollState = {
  left: boolean;
  right: boolean;
};

export default function MealRecordQueue({
  items,
  onRecord,
}: MealRecordQueueProps) {
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const regionId = useId();

  const [scrollState, setScrollState] =
    useState<ScrollState>({
      left: false,
      right: false,
    });

  useEffect(() => {
    const element = scrollRef.current;

    if (!element) {
      return;
    }

    let animationFrame = 0;

    function updateScrollState() {
      if (!element) {
        return;
      }

      const maximumScroll =
        element.scrollWidth - element.clientWidth;

      const nextState = {
        left: element.scrollLeft > 2,
        right:
          maximumScroll > 2 &&
          element.scrollLeft < maximumScroll - 2,
      };

      setScrollState((previous) =>
        previous.left === nextState.left &&
        previous.right === nextState.right
          ? previous
          : nextState
      );
    }

    function scheduleUpdate() {
      cancelAnimationFrame(animationFrame);
      animationFrame = requestAnimationFrame(updateScrollState);
    }

    const observer = new ResizeObserver(scheduleUpdate);

    observer.observe(element);

    for (const child of element.children) {
      observer.observe(child);
    }

    element.addEventListener("scroll", scheduleUpdate, {
      passive: true,
    });

    scheduleUpdate();

    return () => {
      cancelAnimationFrame(animationFrame);
      observer.disconnect();
      element.removeEventListener("scroll", scheduleUpdate);
    };
  }, [items]);

  function scrollCards(direction: -1 | 1) {
    const element = scrollRef.current;

    if (!element) {
      return;
    }

    const reducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;

    element.scrollBy({
      left:
        direction *
        Math.max(272, element.clientWidth * 0.8),
      behavior: reducedMotion ? "auto" : "smooth",
    });
  }

  if (items.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-[var(--color-border)] p-6 text-center">
        <p className="text-sm text-[var(--color-text-secondary)]">
          No customers waiting for meal collection.
        </p>
      </div>
    );
  }

  const arrowClass =
    "absolute top-1/2 z-10 flex h-9 w-7 -translate-y-1/2 items-center justify-center rounded-md bg-transparent text-[var(--color-text-secondary)] transition-colors hover:text-[var(--color-primary)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]";

  return (
    <div className="relative min-w-0">
      {scrollState.left && (
        <button
          type="button"
          aria-label="Scroll customers left"
          aria-controls={regionId}
          onClick={() => scrollCards(-1)}
          className={`${arrowClass} left-0`}
        >
          <ChevronLeft size={24} strokeWidth={2.5} />
        </button>
      )}

      <div
        id={regionId}
        ref={scrollRef}
        role="region"
        aria-label="Customers awaiting meal collection"
        tabIndex={0}
        className="flex min-w-0 snap-x snap-proximity gap-4 overflow-x-auto overscroll-x-contain px-1 py-2 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]"
      >
        {items.map((item) => (
          <div
            key={item.customerId}
            className="w-64 shrink-0 snap-start"
          >
            <MealRecordCard
              item={item}
              onRecord={onRecord}
            />
          </div>
        ))}
      </div>

      {scrollState.right && (
        <button
          type="button"
          aria-label="Scroll customers right"
          aria-controls={regionId}
          onClick={() => scrollCards(1)}
          className={`${arrowClass} right-0`}
        >
          <ChevronRight size={24} strokeWidth={2.5} />
        </button>
      )}
    </div>
  );
}