import {
  Bell,
  CalendarDays,
  CalendarOff,
  CheckCheck,
  CheckCircle2,
  Clock3,
  IndianRupee,
  Receipt,
  Utensils,
} from "lucide-react";

import {
  useEffect,
  useRef,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";
import { toast } from "sonner";

import { useNotifications } from "../hooks";

import type { NotificationType } from "../types";

const CUSTOMER_MENU_PATH = "/customer/menu";
const CUSTOMER_BILLS_PATH = "/customer/my-bills";

const MAX_VISIBLE_NOTIFICATIONS = 10;

function getNotificationIcon(
  notificationType: NotificationType
) {
  switch (notificationType) {
    case "MENU_PUBLISHED":
      return <Utensils size={17} />;

    case "MESS_CLOSURE":
      return <CalendarOff size={17} />;

    case "WEEKLY_SCHEDULE":
      return <CalendarDays size={17} />;

    case "RESPONSE_WINDOW":
      return <Clock3 size={17} />;

    case "MEAL_PRICING":
      return <IndianRupee size={17} />;

    case "BILL_GENERATED":
      return <Receipt size={17} />;

    case "PAYMENT_RECEIVED":
      return <CheckCircle2 size={17} />;

    case "GENERAL":
    default:
      return <Bell size={17} />;
  }
}

function formatNotificationTime(
  value: string
): string {
  const createdAt = new Date(value);

  if (Number.isNaN(createdAt.getTime())) {
    return "";
  }

  const difference = Math.max(
    0,
    Date.now() - createdAt.getTime()
  );

  const seconds = Math.floor(difference / 1000);

  if (seconds < 60) {
    return "Just now";
  }

  const minutes = Math.floor(seconds / 60);

  if (minutes < 60) {
    return `${minutes} min ago`;
  }

  const hours = Math.floor(minutes / 60);

  if (hours < 24) {
    return `${hours} hr${hours === 1 ? "" : "s"} ago`;
  }

  const days = Math.floor(hours / 24);

  if (days === 1) {
    return "Yesterday";
  }

  if (days < 7) {
    return `${days} days ago`;
  }

  return createdAt.toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
  });
}

export default function NotificationBell() {
  const navigate = useNavigate();

  const {
    notifications,
    unreadCount,
    loading,
    markAsRead,
    markAllAsRead,
  } = useNotifications();

  const [open, setOpen] = useState(false);

  const [markingAllAsRead, setMarkingAllAsRead] =
    useState(false);

  const containerRef =
    useRef<HTMLDivElement | null>(null);

  const markingAllRef = useRef(false);

  useEffect(() => {
    function handleClickOutside(
      event: MouseEvent
    ) {
      if (
        containerRef.current &&
        !containerRef.current.contains(
          event.target as Node
        )
      ) {
        setOpen(false);
      }
    }

    function handleKeyDown(
      event: KeyboardEvent
    ) {
      if (event.key === "Escape") {
        setOpen(false);
      }
    }

    document.addEventListener(
      "mousedown",
      handleClickOutside
    );

    document.addEventListener(
      "keydown",
      handleKeyDown
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleClickOutside
      );

      document.removeEventListener(
        "keydown",
        handleKeyDown
      );
    };
  }, []);

  async function handleNotificationClick(
    notificationId: number,
    notificationType: NotificationType,
    read: boolean
  ) {
    const destination =
      notificationType === "MENU_PUBLISHED"
        ? CUSTOMER_MENU_PATH
        : notificationType === "BILL_GENERATED" ||
            notificationType === "PAYMENT_RECEIVED"
          ? CUSTOMER_BILLS_PATH
          : null;

    if (destination) {
      setOpen(false);
      navigate(destination);
    }

    if (!read) {
      try {
        await markAsRead(notificationId);
      } catch (error) {
        toast.error(
          error instanceof Error
            ? error.message
            : "Could not mark the notification as read."
        );
      }
    }
  }

  async function handleMarkAllAsRead() {
    if (
      unreadCount === 0 ||
      markingAllRef.current
    ) {
      return;
    }

    markingAllRef.current = true;
    setMarkingAllAsRead(true);

    try {
      await markAllAsRead();
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : "Could not mark notifications as read."
      );
    } finally {
      markingAllRef.current = false;
      setMarkingAllAsRead(false);
    }
  }

  const visibleNotifications = [...notifications]
    .sort(
      (first, second) =>
        second.createdAt.localeCompare(
          first.createdAt
        ) ||
        second.notificationId - first.notificationId
    )
    .slice(0, MAX_VISIBLE_NOTIFICATIONS);

  return (
    <div
      ref={containerRef}
      className="relative"
    >
      <button
        type="button"
        aria-label={
          unreadCount > 0
            ? `Notifications, ${unreadCount} unread`
            : "Notifications"
        }
        aria-expanded={open}
        onClick={() =>
          setOpen((current) => !current)
        }
        className="relative flex h-10 w-10 items-center justify-center rounded-[var(--radius-md)] text-[var(--color-text-secondary)] transition-colors hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-text)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20"
      >
        <Bell size={21} />

        {!loading && unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex min-h-5 min-w-5 items-center justify-center rounded-full bg-[var(--color-danger)] px-1 text-[10px] font-bold leading-none text-white">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-50 mt-2 w-[23rem] max-w-[calc(100vw-2rem)] overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-[var(--shadow-lg)]">
          <div className="flex items-center justify-between gap-4 border-b border-[var(--color-border)] px-4 py-3.5">
            <div>
              <h3 className="text-sm font-semibold text-[var(--color-text)]">
                Notifications
              </h3>

              <p className="mt-0.5 text-xs text-[var(--color-text-secondary)]">
                {unreadCount > 0
                  ? `${unreadCount} unread`
                  : "You're all caught up"}
              </p>
            </div>

            {unreadCount > 0 && (
              <button
                type="button"
                title="Mark all as read"
                aria-label="Mark all notifications as read"
                disabled={markingAllAsRead}
                onClick={() =>
                  void handleMarkAllAsRead()
                }
                className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-[var(--color-text-secondary)] transition-colors hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-primary)] focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)]/20 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <CheckCheck size={17} />
              </button>
            )}
          </div>

          <div className="max-h-[26rem] overflow-y-auto">
            {loading ? (
              <div className="px-4 py-10 text-center text-sm text-[var(--color-text-secondary)]">
                Loading notifications...
              </div>
            ) : notifications.length === 0 ? (
              <div className="px-6 py-10 text-center">
                <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-full bg-[var(--color-surface-hover)] text-[var(--color-text-secondary)]">
                  <Bell size={19} />
                </div>

                <p className="mt-3 text-sm font-medium text-[var(--color-text)]">
                  No notifications yet
                </p>

                <p className="mt-1 text-xs leading-5 text-[var(--color-text-secondary)]">
                  Important mess updates will appear here.
                </p>
              </div>
            ) : (
              visibleNotifications.map(
                (notification) => (
                  <button
                    key={notification.notificationId}
                    type="button"
                    onClick={() =>
                      void handleNotificationClick(
                        notification.notificationId,
                        notification.notificationType,
                        notification.read
                      )
                    }
                    className={`
                      relative flex w-full gap-3
                      border-b border-[var(--color-border)]
                      px-4 py-3.5 text-left transition-colors
                      last:border-b-0
                      hover:bg-[var(--color-surface-hover)]
                      ${
                        !notification.read
                          ? "bg-[var(--color-primary)]/[0.06]"
                          : "bg-[var(--color-surface)]"
                      }
                    `}
                  >
                    <div
                      className={`
                        mt-0.5 flex h-9 w-9 shrink-0
                        items-center justify-center rounded-lg
                        ${
                          !notification.read
                            ? "bg-[var(--color-primary)]/12 text-[var(--color-primary)]"
                            : "bg-[var(--color-surface-hover)] text-[var(--color-text-secondary)]"
                        }
                      `}
                    >
                      {getNotificationIcon(
                        notification.notificationType
                      )}
                    </div>

                    <div className="min-w-0 flex-1">
                      <div className="flex items-start justify-between gap-3">
                        <p
                          className={`
                            min-w-0 text-sm text-[var(--color-text)]
                            ${
                              !notification.read
                                ? "font-semibold"
                                : "font-medium"
                            }
                          `}
                        >
                          {notification.title}
                        </p>

                        {!notification.read && (
                          <span
                            className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-[var(--color-primary)]"
                            aria-label="Unread"
                          />
                        )}
                      </div>

                      <p className="mt-1 text-sm leading-5 text-[var(--color-text-secondary)]">
                        {notification.message}
                      </p>

                      <p
                        className={`
                          mt-1.5 text-[11px]
                          ${
                            !notification.read
                              ? "font-medium text-[var(--color-primary)]"
                              : "text-[var(--color-text-secondary)]"
                          }
                        `}
                      >
                        {formatNotificationTime(
                          notification.createdAt
                        )}
                      </p>
                    </div>
                  </button>
                )
              )
            )}
          </div>
        </div>
      )}
    </div>
  );
}