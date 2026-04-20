interface ConfirmModalProps {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  busy?: boolean;
  onConfirm: () => void | Promise<void>;
  onCancel: () => void;
  testId?: string;
}

export function ConfirmModal({
  title,
  message,
  confirmLabel = "Yes",
  cancelLabel = "No",
  busy = false,
  onConfirm,
  onCancel,
  testId = "confirm-modal"
}: ConfirmModalProps) {
  return (
    <div className="modal-backdrop" data-testid={testId}>
      <div className="modal-card modal-card-compact">
        <div className="stack gap-md">
          <div className="section-heading">
            <h2>{title}</h2>
          </div>
          <p>{message}</p>
          <div className="action-row">
            <button type="button" className="ghost-button" onClick={onCancel}>
              {cancelLabel}
            </button>
            <button type="button" className="primary-button" disabled={busy} onClick={() => void onConfirm()}>
              {busy ? "Working..." : confirmLabel}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
