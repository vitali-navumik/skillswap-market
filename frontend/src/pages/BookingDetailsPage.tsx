import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useLocation, useParams } from "react-router-dom";
import { ConfirmModal } from "../components/ConfirmModal";
import { DeleteIcon, EditIcon } from "../components/ActionIcons";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import { formatRatingValue, formatReviewTimestamp } from "../lib/ratings";
import type { AuthUser, Booking, Review } from "../types";

interface BookingDetailsPageProps {
  token: string | null;
  user: AuthUser | null;
}

function getStatusClass(value: string) {
  return value.toLowerCase().replace(/_/g, "-");
}

function StarRating({ value }: { value: number }) {
  const clampedValue = Math.max(0, Math.min(5, value));
  const fillWidth = `${(clampedValue / 5) * 100}%`;

  return (
    <span
      className="star-rating"
      aria-label={`${formatRatingValue(clampedValue)} out of 5 stars`}
      title={`${formatRatingValue(clampedValue)} out of 5`}
    >
      <span className="star-rating-base">★★★★★</span>
      <span className="star-rating-fill" style={{ width: fillWidth }}>
        ★★★★★
      </span>
    </span>
  );
}

export function BookingDetailsPage({ token, user }: BookingDetailsPageProps) {
  const location = useLocation();
  const params = useParams();
  const bookingPublicId = params.bookingPublicId ?? "";
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [feedback, setFeedback] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busyAction, setBusyAction] = useState<string | null>(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [deletingReview, setDeletingReview] = useState(false);
  const [showDeleteReviewConfirm, setShowDeleteReviewConfirm] = useState(false);
  const locationState = (location.state as { flash?: string } | null) ?? null;

  const load = async () => {
    if (!token || !bookingPublicId) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const [bookingResult, reviewResult] = await Promise.all([
        api.listBookings(token),
        api.listReviews(token)
      ]);
      const currentBooking = bookingResult.find((entry) => entry.publicId === bookingPublicId) ?? null;
      const mentorVisibleReviews = currentBooking && currentBooking.mentorId === user?.id
        ? await api.getOfferReviews(currentBooking.offerPublicId)
        : [];
      const accessibleReviews = [...reviewResult];
      mentorVisibleReviews.forEach((entry) => {
        if (!accessibleReviews.some((reviewEntry) => reviewEntry.id === entry.id)) {
          accessibleReviews.push(entry);
        }
      });
      setBookings(bookingResult);
      setReviews(accessibleReviews);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load booking");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [bookingPublicId, token, user]);

  const booking = useMemo(() => bookings.find((entry) => entry.publicId === bookingPublicId) ?? null, [bookings, bookingPublicId]);
  const review = useMemo(() => reviews.find((entry) => entry.bookingId === booking?.id) ?? null, [reviews, booking]);

  const runAction = async (action: "cancel" | "complete") => {
    if (!token || !booking) {
      return;
    }

    setBusyAction(action);
    setFeedback(null);
    setError("");

    try {
      if (action === "cancel") {
        await api.cancelBooking(token, booking.publicId);
      } else {
        await api.completeBooking(token, booking.publicId);
      }

      setFeedback({
        tone: "success",
        text:
          action === "cancel"
            ? "Booking cancelled."
            : "Booking completed."
      });
      await load();
    } catch (err) {
      setFeedback({
        tone: "error",
        text: err instanceof ApiError ? err.message : `Failed to ${action} booking`
      });
    } finally {
      setBusyAction(null);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/bookings/${bookingPublicId}` }} />;
  }

  if (loading) {
    return <div className="panel" data-testid="booking-details-loading">Loading booking details...</div>;
  }

  if (!booking) {
    return <div className="panel" data-testid="booking-details-not-found">Booking not found.</div>;
  }

  const isStudent = booking.studentId === user.id;
  const isMentor = booking.mentorId === user.id;
  const isFutureReserved = booking.status === "RESERVED" && new Date(booking.slotStartTime).getTime() > Date.now();
  const reviewStatusLabel = review
    ? "Reviewed"
    : isStudent && booking.status === "COMPLETED"
      ? "Review pending"
      : isMentor
        ? "Waiting for review"
        : "Not available yet";
  const canCancel = (isFutureReserved && (isStudent || isMentor)) || (booking.status === "COMPLETED" && isMentor);
  const canComplete = booking.status === "RESERVED" && isMentor;
  const canManageReview = Boolean(user && review && (user.roles.includes("ADMIN") || user.id === review.authorId));
  const counterpartLabel = isStudent && !isMentor ? "Mentor" : isMentor && !isStudent ? "Student" : null;
  const counterpartValue = counterpartLabel === "Mentor"
    ? booking.mentorDisplayName
    : counterpartLabel === "Student"
      ? booking.studentDisplayName
      : null;

  return (
    <div className="stack gap-xl detail-shell booking-details-page" data-testid="booking-details-page">
      <BackButton fallbackTo="/bookings" testId="booking-details-back-link" />

      <section className="panel section-stack booking-details-panel">
        <div className="stack gap-sm">
          <span className="eyebrow">Booking</span>
          <div className="section-heading booking-details-heading-row">
            <h1 className="booking-details-heading">{booking.offerTitle}</h1>
            <span className={`badge subtle status-chip status-${getStatusClass(booking.status)}`}>
              {booking.status}
            </span>
          </div>
        </div>
        {locationState?.flash ? <div className="alert success" data-testid="booking-details-flash">{locationState.flash}</div> : null}
        {feedback ? <div className={`alert ${feedback.tone}`} data-testid="booking-details-feedback">{feedback.text}</div> : null}
        {error ? <div className="alert error" data-testid="booking-details-error">{error}</div> : null}
        <div className="booking-details-facts" data-testid="booking-details-summary">
          <div className="booking-fact-row">
            <span className="label">Date &amp; Time:</span>
            <span>
              {new Date(booking.slotStartTime).toLocaleDateString()}{" "}
              {new Date(booking.slotStartTime).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit"
              })}{" "}
              -{" "}
              {new Date(booking.slotEndTime).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit"
              })}
            </span>
          </div>
          {counterpartLabel ? (
            <div className="booking-fact-row">
              <span className="label">{counterpartLabel}:</span>
              <span>{counterpartValue}</span>
            </div>
          ) : null}
          <div className="booking-fact-row">
            <span className="label">Price:</span>
            <span>{booking.priceCredits} credits</span>
          </div>
        </div>
        <div className="action-row">
          {canCancel ? (
            <button
              type="button"
              className="ghost-button"
              data-testid="booking-details-cancel"
              disabled={busyAction === "cancel"}
              onClick={() => setShowCancelConfirm(true)}
            >
              {busyAction === "cancel" ? "Cancelling..." : "Cancel Booking"}
            </button>
          ) : null}
          {canComplete ? (
            <button
              type="button"
              className="primary-button"
              data-testid="booking-details-complete"
              disabled={busyAction === "complete"}
              onClick={() => void runAction("complete")}
            >
              {busyAction === "complete" ? "Completing..." : "Complete"}
            </button>
          ) : null}
        </div>
      </section>

      {showCancelConfirm ? (
        <div className="modal-backdrop" data-testid="booking-cancel-confirm-modal">
          <div className="modal-card modal-card-compact">
            <div className="stack gap-md">
              <div className="section-heading">
                <h2>Cancel Booking</h2>
              </div>
              <p>Do you want to cancel this booking?</p>
              <div className="action-row">
                <button
                  type="button"
                  className="ghost-button"
                  data-testid="booking-cancel-confirm-no"
                  onClick={() => setShowCancelConfirm(false)}
                >
                  No
                </button>
                <button
                  type="button"
                  className="primary-button"
                  data-testid="booking-cancel-confirm-yes"
                  disabled={busyAction === "cancel"}
                  onClick={async () => {
                    setShowCancelConfirm(false);
                    await runAction("cancel");
                  }}
                >
                  {busyAction === "cancel" ? "Cancelling..." : "Yes"}
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : null}

      <section className="panel section-stack" data-testid="booking-details-review-panel">
        <div className="section-heading">
          <h2>Booking Review</h2>
          {review ? (
            <div className="icon-action-group">
              {canManageReview ? (
                <Link
                  className="icon-action-link"
                  data-testid="booking-review-open-edit-page"
                  to={`/reviews/${review.publicId}/edit`}
                  aria-label="Edit review"
                  title="Edit review"
                >
                  <EditIcon />
                </Link>
              ) : null}
              {canManageReview ? (
                <button
                  type="button"
                  className="icon-action-button"
                  data-testid="booking-review-delete-button"
                  disabled={deletingReview}
                  onClick={() => setShowDeleteReviewConfirm(true)}
                  aria-label="Delete review"
                  title="Delete review"
                >
                  <DeleteIcon />
                </button>
              ) : null}
            </div>
          ) : (
            <span>{reviewStatusLabel}</span>
          )}
        </div>
        {review ? (
          <article className="review-card" data-testid="booking-review-card">
            <strong>{review.offerTitle}</strong>
            <span className="offer-review-author">{review.authorDisplayName}</span>
            <div className="offer-review-score-row">
              <StarRating value={review.rating} />
            </div>
            <small className="muted">{formatReviewTimestamp(review.createdAt, review.updatedAt)}</small>
            <p>{review.comment}</p>
          </article>
        ) : isStudent && booking.status === "COMPLETED" ? (
          <div className="empty-state">
            <strong>No review for this booking yet.</strong>
            <span>You can leave one review for a completed booking.</span>
            <div className="action-row">
              <Link
                className="ghost-button"
                data-testid="booking-details-create-review"
                to={`/bookings/${booking.publicId}/review/new`}
              >
                Leave review
              </Link>
            </div>
          </div>
        ) : (
          <div className="empty-state">
            <strong>{isMentor ? "Review has not been submitted yet." : "Review is not available here yet."}</strong>
            <span>
              {isMentor
                ? "Reviews appear here after a review is submitted for this completed booking."
                : "Student reviews become available after a completed session."}
            </span>
          </div>
        )}
      </section>

      {showDeleteReviewConfirm && review ? (
        <ConfirmModal
          title="Delete Review"
          message="Do you want to delete this review?"
          confirmLabel={deletingReview ? "Deleting..." : "Yes"}
          busy={deletingReview}
          onCancel={() => setShowDeleteReviewConfirm(false)}
          onConfirm={async () => {
            if (!token) {
              return;
            }
            setShowDeleteReviewConfirm(false);
            setDeletingReview(true);
            setError("");
            setFeedback(null);
            try {
              await api.deleteReview(token, review.publicId);
              setFeedback({ tone: "success", text: "Review deleted." });
              await load();
            } catch (err) {
              setFeedback({
                tone: "error",
                text: err instanceof ApiError ? err.message : "Failed to delete review"
              });
            } finally {
              setDeletingReview(false);
            }
          }}
          testId="booking-review-delete-confirm-modal"
        />
      ) : null}
    </div>
  );
}
