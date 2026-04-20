import { FormEvent, useEffect, useMemo, useState } from "react";
import { Navigate, useParams } from "react-router-dom";
import { DeleteIcon } from "../components/ActionIcons";
import { BackButton } from "../components/BackButton";
import { ConfirmModal } from "../components/ConfirmModal";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Offer, Slot } from "../types";

interface EditOfferPageProps {
  token: string | null;
  user: AuthUser | null;
  detailsPathBuilder?: (offerPublicId: string) => string;
  forceAdminMode?: boolean;
  workspaceLabel?: string;
}

const projectCategories = ["Career", "Databases", "Frontend", "Operations"] as const;
const statusOptions: Offer["status"][] = ["DRAFT", "ACTIVE", "ARCHIVED", "BLOCKED"];
const timeOptions = Array.from({ length: 24 * 6 }, (_, index) => {
  const totalMinutes = index * 10;
  const hours = String(Math.floor(totalMinutes / 60)).padStart(2, "0");
  const minutes = String(totalMinutes % 60).padStart(2, "0");
  return `${hours}:${minutes}`;
});

function formatSlotDate(value: string) {
  return new Date(value).toLocaleDateString();
}

function formatSlotTimeRange(startTime: string, endTime: string) {
  return `${new Date(startTime).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit"
  })} - ${new Date(endTime).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit"
  })}`;
}

export function EditOfferPage({
  token,
  user,
  detailsPathBuilder,
  forceAdminMode = false,
  workspaceLabel
}: EditOfferPageProps) {
  const params = useParams();
  const offerPublicId = params.offerPublicId!;
  const [offer, setOffer] = useState<Offer | null>(null);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [offerForm, setOfferForm] = useState({
    title: "",
    description: "",
    category: "",
    durationMinutes: 60,
    priceCredits: 20,
    status: "DRAFT" as Offer["status"]
  });
  const [offerMessage, setOfferMessage] = useState("");
  const [offerSaving, setOfferSaving] = useState(false);
  const [slotDate, setSlotDate] = useState("");
  const [slotStart, setSlotStart] = useState("00:00");
  const [slotEnd, setSlotEnd] = useState("00:00");
  const [slotMessage, setSlotMessage] = useState("");
  const [slotLoading, setSlotLoading] = useState(false);
  const [deletingSlotId, setDeletingSlotId] = useState<number | null>(null);
  const [slotPendingDeleteId, setSlotPendingDeleteId] = useState<number | null>(null);

  const load = async () => {
    setLoading(true);
    setError("");

    try {
      const [offerResult, slotResult] = await Promise.all([
        api.getOffer(offerPublicId),
        api.getSlots(offerPublicId)
      ]);
      setOffer(offerResult);
      setOfferForm({
        title: offerResult.title,
        description: offerResult.description,
        category: offerResult.category,
        durationMinutes: offerResult.durationMinutes,
        priceCredits: offerResult.priceCredits,
        status: offerResult.status
      });
      setSlots(slotResult);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load offer");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [offerPublicId]);

  const isAdmin = forceAdminMode || Boolean(user?.roles.includes("ADMIN"));
  const detailsPath = offer ? (detailsPathBuilder ? detailsPathBuilder(offer.publicId) : `/offers/${offer.publicId}`) : "/offers";
  const canManageOffer = Boolean(
    user &&
      offer &&
      (isAdmin || (user.id === offer.mentorId && user.roles.includes("MENTOR")))
  );

  const sortedSlots = useMemo(
    () =>
      [...slots].sort(
        (left, right) => new Date(right.startTime).getTime() - new Date(left.startTime).getTime()
      ),
    [slots]
  );

  const onSaveOffer = async (event: FormEvent) => {
    event.preventDefault();
    if (!token || !offer || !canManageOffer) {
      return;
    }

    setOfferSaving(true);
    setOfferMessage("");
    try {
      await api.updateOffer(token, offer.publicId, {
        title: offerForm.title,
        description: offerForm.description,
        category: offerForm.category,
        durationMinutes: offerForm.durationMinutes,
        priceCredits: offerForm.priceCredits,
        status: offerForm.status
      });
      setOfferMessage("Offer updated.");
      await load();
    } catch (err) {
      setOfferMessage(err instanceof ApiError ? err.message : "Failed to update offer");
    } finally {
      setOfferSaving(false);
    }
  };

  const onCreateSlot = async (event: FormEvent) => {
    event.preventDefault();
    if (!token || !offer || !canManageOffer) {
      return;
    }

    setSlotLoading(true);
    setSlotMessage("");

    try {
      const createdSlots = await api.createSlot(token, offer.publicId, {
        date: slotDate,
        startTime: slotStart,
        endTime: slotEnd
      });
      setSlotMessage(`${createdSlots.length} slot${createdSlots.length === 1 ? "" : "s"} created.`);
      setSlotDate("");
      setSlotStart("00:00");
      setSlotEnd("00:00");
      await load();
    } catch (err) {
      setSlotMessage(err instanceof ApiError ? err.message : "Failed to create slot");
    } finally {
      setSlotLoading(false);
    }
  };

  const onDeleteSlot = async (slotId: number) => {
    if (!token || !canManageOffer) {
      return;
    }

    setDeletingSlotId(slotId);
    setSlotMessage("");

    try {
      await api.deleteSlot(token, slotId);
      setSlotMessage("Slot deleted.");
      await load();
    } catch (err) {
      setSlotMessage(err instanceof ApiError ? err.message : "Failed to delete slot");
    } finally {
      setDeletingSlotId(null);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/offers/${offerPublicId}/edit` }} />;
  }

  if (loading) {
    return <div className="panel" data-testid="edit-offer-loading">Loading offer editor...</div>;
  }

  if (!offer) {
    return <div className="panel" data-testid="edit-offer-not-found">Offer not found.</div>;
  }

  if (!canManageOffer) {
    return (
      <div className="panel stack gap-md" data-testid="edit-offer-forbidden">
        <BackButton fallbackTo={detailsPath} testId="edit-offer-forbidden-back-link" />
        <strong>You do not have permission to edit this offer.</strong>
      </div>
    );
  }

  return (
    <div className="stack gap-xl detail-shell offer-editor-page" data-testid="edit-offer-page">
      <BackButton fallbackTo={detailsPath} testId="back-to-offer-details" />
      {error ? <div className="alert error" data-testid="edit-offer-error">{error}</div> : null}

      <section className="panel stack gap-md offer-editor-form" data-testid="offer-manage-panel">
        <form className="stack gap-md" onSubmit={onSaveOffer}>
          <div className="section-heading">
            <h1>Edit offer</h1>
          </div>

          <label>
            Title
            <input
              data-testid="edit-offer-title-input"
              value={offerForm.title}
              onChange={(event) => setOfferForm((current) => ({ ...current, title: event.target.value }))}
              required
            />
          </label>

          <label>
            Description
            <textarea
              data-testid="edit-offer-description-input"
              rows={4}
              value={offerForm.description}
              onChange={(event) => setOfferForm((current) => ({ ...current, description: event.target.value }))}
              required
            />
          </label>

          <div className="offer-editor-four-col">
            <label className="offer-editor-select-field offer-editor-select-field-category">
              Category
              <select
                data-testid="edit-offer-category-input"
                value={offerForm.category}
                onChange={(event) => setOfferForm((current) => ({ ...current, category: event.target.value }))}
                required
              >
                {projectCategories.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
            <label className="offer-editor-select-field offer-editor-select-field-status">
              Status
              <select
                data-testid="edit-offer-status-select"
                value={offerForm.status}
                onChange={(event) =>
                  setOfferForm((current) => ({ ...current, status: event.target.value as Offer["status"] }))
                }
              >
                {statusOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
            <label className="offer-editor-number-field offer-editor-number-field-duration">
              Duration (minutes)
              <input
                data-testid="edit-offer-duration-input"
                type="number"
                min={1}
                value={offerForm.durationMinutes}
                onChange={(event) =>
                  setOfferForm((current) => ({ ...current, durationMinutes: Number(event.target.value) }))
                }
                required
              />
            </label>
            <label className="offer-editor-number-field offer-editor-number-field-price">
              Price (credits)
              <input
                data-testid="edit-offer-price-input"
                type="number"
                min={1}
                value={offerForm.priceCredits}
                onChange={(event) =>
                  setOfferForm((current) => ({ ...current, priceCredits: Number(event.target.value) }))
                }
                required
              />
            </label>
          </div>

          {offerMessage ? (
            <div
              className={`alert ${offerMessage.includes("updated") ? "success" : "error"}`}
              data-testid="offer-manage-feedback"
            >
              {offerMessage}
            </div>
          ) : null}

          <div className="action-row">
            <button className="primary-button offer-editor-submit" type="submit" disabled={offerSaving} data-testid="save-offer-button">
              {offerSaving ? "Saving..." : "Save Offer"}
            </button>
          </div>
        </form>
      </section>

      <section className="panel stack gap-md offer-editor-form" data-testid="offer-slot-management-panel">
        <div className="section-heading">
          <h2>Slot management</h2>
          <span>Create slots from one date and time range</span>
        </div>

        <form className="stack gap-md" onSubmit={onCreateSlot}>
          <div className="offer-slot-inputs">
            <label>
              Date
              <input
                data-testid="create-slot-date-input"
                type="date"
                value={slotDate}
                onChange={(event) => setSlotDate(event.target.value)}
                required
              />
            </label>

            <label className="offer-slot-time-field">
              Start time
              <select
                data-testid="create-slot-start-input"
                value={slotStart}
                onChange={(event) => setSlotStart(event.target.value)}
                required
              >
                {timeOptions.map((option) => (
                  <option key={`start-${option}`} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>

            <label className="offer-slot-time-field">
              End time
              <select
                data-testid="create-slot-end-input"
                value={slotEnd}
                onChange={(event) => setSlotEnd(event.target.value)}
                required
              >
                {timeOptions.map((option) => (
                  <option key={`end-${option}`} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
          </div>

          {slotMessage ? (
            <div
              className={`alert ${slotMessage.includes("created") || slotMessage === "Slot deleted." ? "success" : "error"}`}
              data-testid="offer-slot-feedback"
            >
              {slotMessage}
            </div>
          ) : null}

          <button className="primary-button offer-editor-submit" type="submit" disabled={slotLoading} data-testid="create-slot-submit">
            {slotLoading ? "Saving slots..." : "Add Slot"}
          </button>
        </form>

        {sortedSlots.length === 0 ? (
          <p className="muted" data-testid="edit-offer-slots-empty-state">No slots yet.</p>
        ) : (
          <div className="stack gap-md">
            <div className="section-heading">
              <h3>Existing slots</h3>
              <span>{sortedSlots.length} slot(s)</span>
            </div>
            <div className="bookings-table-wrap offer-slot-table-wrap" data-testid="edit-offer-slots-table-wrap">
              <table className="bookings-table" data-testid="edit-offer-slots-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {sortedSlots.map((slot) => {
                    const canDeleteSlot = slot.status === "OPEN" && new Date(slot.startTime).getTime() > Date.now();
                    return (
                      <tr key={`manage-${slot.id}`}>
                        <td>{formatSlotDate(slot.startTime)}</td>
                        <td>{formatSlotTimeRange(slot.startTime, slot.endTime)}</td>
                        <td>
                          <span className="badge subtle">{slot.status}</span>
                        </td>
                        <td>
                          {canDeleteSlot ? (
                            <button
                              type="button"
                              className="icon-action-button"
                              data-testid={`delete-slot-button-${slot.id}`}
                              disabled={deletingSlotId === slot.id}
                              onClick={() => setSlotPendingDeleteId(slot.id)}
                              aria-label="Delete slot"
                              title="Delete slot"
                            >
                              <DeleteIcon />
                            </button>
                          ) : (
                            <span className="muted">-</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </section>

      {slotPendingDeleteId !== null ? (
        <ConfirmModal
          title="Delete Slot"
          message="Do you want to delete this slot?"
          confirmLabel={deletingSlotId === slotPendingDeleteId ? "Deleting..." : "Yes"}
          busy={deletingSlotId === slotPendingDeleteId}
          onCancel={() => setSlotPendingDeleteId(null)}
          onConfirm={async () => {
            const slotId = slotPendingDeleteId;
            setSlotPendingDeleteId(null);
            if (slotId !== null) {
              await onDeleteSlot(slotId);
            }
          }}
          testId="delete-slot-confirm-modal"
        />
      ) : null}
    </div>
  );
}
