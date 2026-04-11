type ToastTone = "success" | "error" | "info";

type ToastProps = {
  message: string;
  tone?: ToastTone;
  actionLabel?: string;
  onAction?: () => void;
  onClose?: () => void;
  durationMs?: number;
};

export function Toast({
  message,
  tone = "info",
  actionLabel,
  onAction,
  onClose,
  durationMs = 2600,
}: ToastProps) {
  return (
    <div className="toast-viewport" role="status" aria-live="polite">
      <div
        className={`toast toast--${tone}`}
        style={{ ["--toast-duration" as string]: `${durationMs}ms` }}
      >
        <span className="toast__message">{message}</span>
        {actionLabel && onAction ? (
          <button type="button" className="toast__action" onClick={onAction}>
            {actionLabel}
          </button>
        ) : null}
        {onClose ? (
          <button
            type="button"
            className="toast__close"
            aria-label="알림 닫기"
            onClick={onClose}
          >
            ×
          </button>
        ) : null}
      </div>
    </div>
  );
}
