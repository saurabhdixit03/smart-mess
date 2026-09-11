import {
  Bell,
  CalendarDays,
  CalendarOff,
  Utensils,
} from "lucide-react";

import {
  useEffect,
  useRef,
  useState,
} from "react";

import { useNavigate } from "react-router-dom";

import { useNotifications } from "../hooks";

const CUSTOMER_MENU_PATH =
  "/customer/menu";

function getNotificationIcon(
  notificationType: string
) {
  switch (notificationType) {
    case "MENU_PUBLISHED":
      return <Utensils size={17} />;

    case "MESS_CLOSURE":
      return <CalendarOff size={17} />;

    case "WEEKLY_SCHEDULE":
      return <CalendarDays size={17} />;

    default:
      return <Bell size={17} />;
  }
}

function formatNotificationTime(
  value: string
) {
  const createdAt =
    new Date(value);

  const now =
    new Date();

  const difference =
    now.getTime() -
    createdAt.getTime();

  const seconds =
    Math.floor(
      difference / 1000
    );

  if (seconds < 60) {
    return "Just now";
  }

  const minutes =
    Math.floor(
      seconds / 60
    );

  if (minutes < 60) {
    return `${minutes} min ago`;
  }

  const hours =
    Math.floor(
      minutes / 60
    );

  if (hours < 24) {
    return `${hours} hr${
      hours === 1 ? "" : "s"
    } ago`;
  }

  const days =
    Math.floor(
      hours / 24
    );

  if (days === 1) {
    return "Yesterday";
  }

  if (days < 7) {
    return `${days} days ago`;
  }

  return createdAt.toLocaleDateString(
    undefined,
    {
      day: "numeric",
      month: "short",
    }
  );
}

export default function NotificationBell() {
  const navigate =
    useNavigate();

  const {
    notifications,
    unreadCount,
    loading,
    markAsRead,
  } = useNotifications();

  const [open, setOpen] =
    useState(false);

  const containerRef =
    useRef<HTMLDivElement | null>(
      null
    );

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

    document.addEventListener(
      "mousedown",
      handleClickOutside
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleClickOutside
      );
    };
  }, []);

  async function handleNotificationClick(
    notificationId: number,
    notificationType: string,
    read: boolean
  ) {
    if (!read) {
      await markAsRead(
        notificationId
      );
    }

    if (
      notificationType ===
      "MENU_PUBLISHED"
    ) {
      setOpen(false);

      navigate(
        CUSTOMER_MENU_PATH
      );
    }
  }

  return (
    <div
      ref={containerRef}
      className="relative"
    >
      {/* Notification Trigger */}
      <button
        type="button"
        aria-label="Notifications"
        aria-expanded={open}
        onClick={() =>
          setOpen(
            (current) => !current
          )
        }
        className="
          relative
          flex
          h-10
          w-10
          items-center
          justify-center
          rounded-[var(--radius-md)]
          text-[var(--color-text-secondary)]
          transition-colors
          hover:bg-[var(--color-surface-hover)]
          hover:text-[var(--color-text)]
          focus:outline-none
          focus:ring-2
          focus:ring-[var(--color-primary)]/20
        "
      >
        <Bell size={21} />

        {!loading &&
          unreadCount > 0 && (
            <span
              className="
                absolute
                -right-0.5
                -top-0.5
                flex
                min-h-5
                min-w-5
                items-center
                justify-center
                rounded-full
                bg-[var(--color-danger)]
                px-1
                text-[10px]
                font-bold
                leading-none
                text-white
              "
            >
              {unreadCount > 99
                ? "99+"
                : unreadCount}
            </span>
          )}
      </button>

      {/* Notification Panel */}
      {open && (
        <div
          className="
            absolute
            right-0
            z-50
            mt-2
            w-[23rem]
            max-w-[calc(100vw-2rem)]
            overflow-hidden
            rounded-xl
            border
            border-[var(--color-border)]
            bg-[var(--color-surface)]
            shadow-[var(--shadow-lg)]
          "
        >
          {/* Header */}
          <div
            className="
              border-b
              border-[var(--color-border)]
              px-4
              py-3.5
            "
          >
            <h3
              className="
                text-sm
                font-semibold
                text-[var(--color-text)]
              "
            >
              Notifications
            </h3>

            <p
              className="
                mt-0.5
                text-xs
                text-[var(--color-text-secondary)]
              "
            >
              {unreadCount > 0
                ? `${unreadCount} unread`
                : "You're all caught up"}
            </p>
          </div>

          {/* Notification List */}
          <div
            className="
              max-h-[26rem]
              overflow-y-auto
            "
          >
            {loading ? (
              <div
                className="
                  px-4
                  py-10
                  text-center
                  text-sm
                  text-[var(--color-text-secondary)]
                "
              >
                Loading notifications...
              </div>
            ) : notifications.length ===
              0 ? (
              <div
                className="
                  px-6
                  py-10
                  text-center
                "
              >
                <div
                  className="
                    mx-auto
                    flex
                    h-10
                    w-10
                    items-center
                    justify-center
                    rounded-full
                    bg-[var(--color-surface-hover)]
                    text-[var(--color-text-secondary)]
                  "
                >
                  <Bell size={19} />
                </div>

                <p
                  className="
                    mt-3
                    text-sm
                    font-medium
                    text-[var(--color-text)]
                  "
                >
                  No notifications yet
                </p>

                <p
                  className="
                    mt-1
                    text-xs
                    leading-5
                    text-[var(--color-text-secondary)]
                  "
                >
                  Important mess updates
                  will appear here.
                </p>
              </div>
            ) : (
              notifications.map(
                (notification) => (
                  <button
                    key={
                      notification.notificationId
                    }
                    type="button"
                    onClick={() =>
                      handleNotificationClick(
                        notification.notificationId,
                        notification.notificationType,
                        notification.read
                      )
                    }
                    className={`
                      relative
                      flex
                      w-full
                      gap-3
                      border-b
                      border-[var(--color-border)]
                      px-4
                      py-3.5
                      text-left
                      transition-colors
                      last:border-b-0
                      hover:bg-[var(--color-surface-hover)]
                      ${
                        !notification.read
                          ? "bg-[var(--color-primary)]/[0.06]"
                          : "bg-[var(--color-surface)]"
                      }
                    `}
                  >
                    {/* Notification Icon */}
                    <div
                      className={`
                        mt-0.5
                        flex
                        h-9
                        w-9
                        shrink-0
                        items-center
                        justify-center
                        rounded-lg
                        transition-colors
                        ${
                          !notification.read
                            ? `
                              bg-[var(--color-primary)]/12
                              text-[var(--color-primary)]
                            `
                            : `
                              bg-[var(--color-surface-hover)]
                              text-[var(--color-text-secondary)]
                            `
                        }
                      `}
                    >
                      {getNotificationIcon(
                        notification.notificationType
                      )}
                    </div>

                    {/* Notification Content */}
                    <div className="min-w-0 flex-1">
                      <div
                        className="
                          flex
                          items-start
                          justify-between
                          gap-3
                        "
                      >
                        <p
                          className={`
                            min-w-0
                            text-sm
                            text-[var(--color-text)]
                            ${
                              !notification.read
                                ? "font-semibold"
                                : "font-medium"
                            }
                          `}
                        >
                          {
                            notification.title
                          }
                        </p>

                        {!notification.read && (
                          <span
                            className="
                              mt-1.5
                              h-2
                              w-2
                              shrink-0
                              rounded-full
                              bg-[var(--color-primary)]
                            "
                            aria-label="Unread"
                          />
                        )}
                      </div>

                      <p
                        className="
                          mt-1
                          text-sm
                          leading-5
                          text-[var(--color-text-secondary)]
                        "
                      >
                        {
                          notification.message
                        }
                      </p>

                      <p
                        className={`
                          mt-1.5
                          text-[11px]
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