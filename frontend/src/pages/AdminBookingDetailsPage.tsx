import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useLocation, useParams } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { ConfirmModal } from "../components/ConfirmModal";
import { DeleteIcon, EditIcon } from "../components/ActionIcons";
import { api, ApiError } from "../lib/api";
import { formatRatingValue, formatReviewTimestamp } from "../lib/ratings";
import type { AuthUser, Booking, Review } from "../types";

interface AdminBookingDetailsPageProps {
  token: string | null;
  user: AuthUser | null;
}

function getStatusClass(value: string) {
  return value.toLowerCase().replace(/_/g, "-");
}

function StarRating({ value }: { value: number }) {
  const clampedValue = Math.max(0, Math.min(5, value));
  const fillWidth = `${(clampedValue / 5) * 100}%`;
  const stars = "\u2605\u2605\u2605\u2605\u2605";

  return (
    <span
      className="star-rating"
      aria-label={`${formatRatingValue(clampedValue)} out of 5 stars`}
      title={`${formatRatingValue(clampedValue)} out of 5`}
    >
      <span className="star-rating-base">{stars}</span>
      <span className="star-rating-fill" style={{ width: fillWidth }}>
        {stars}
      </span>
    </span>
  );
}

export function AdminBookingDetailsPage({ token, user }: AdminBookingDetailsPageProps) {
  const params = useParams();
  const location = useLocation();
  const bookingPublicId = params.bookingPublicId ?? "";
  const locationState = (location.state as { flash?: string } | null) ?? null;
  const [booking, setBooking] = useState<Booking | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busyAction, setBusyAction] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [pendingDeleteReview, setPendingDeleteReview] = useState<Review | null>(null);
  const [deletingReviewId, setDeletingReviewId] = useState<number | null>(null);

  const load = async () => {
    if (!token || !bookingPublicId) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const [bookingResult, reviewsResult] = await Promise.all([
        api.getBooking(token, bookingPublicId),
        api.listReviews(token)
      ]);
      setBooking(bookingResult);
      setReviews(reviewsResult);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load booking");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [bookingPublicId, token]);

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
      } else if (action === "complete") {
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

  const bookingReviews = useMemo(
    () => reviews.filter((review) => review.bookingId === booking?.id),
    [booking, reviews]
  );

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/bookings/${bookingPublicId}` }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-booking-details-loading">Loading booking details...</div>;
  }

  if (!booking) {
    return <div className="panel" data-testid="admin-booking-details-not-found">Booking not found.</div>;
  }

  const canCancel =
    (booking.status === "RESERVED" && new Date(booking.slotStartTime).getTime() > Date.now()) ||
    booking.status === "COMPLETED";
  const canComplete = booking.status === "RESERVED";

  return (
    <div className="stack gap-xl detail-shell booking-details-page" data-testid="admin-booking-details-page">
      <BackButton fallbackTo="/bookings" testId="admin-booking-back-link" />
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

        {locationState?.flash ? <div className="alert success" data-testid="admin-booking-details-flash">{locationState.flash}</div> : null}
        {feedback ? <div className={`alert ${feedback.tone}`} data-testid="admin-booking-details-feedback">{feedback.text}</div> : null}
        {error ? <div className="alert error" data-testid="admin-booking-details-error">{error}</div> : null}

        <div className="booking-details-facts" data-testid="admin-booking-details-summary">
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
          <div className="booking-fact-row">
            <span className="label">Student:</span>
            <span>{booking.studentDisplayName}</span>
          </div>
          <div className="booking-fact-row">
            <span className="label">Mentor:</span>
            <span>{booking.mentorDisplayName}</span>
          </div>
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
              data-testid="admin-booking-cancel"
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
              data-testid="admin-booking-complete"
              disabled={busyAction === "complete"}
              onClick={() => void runAction("complete")}
            >
              {busyAction === "complete" ? "Completing..." : "Complete"}
            </button>
          ) : null}
        </div>
      </section>

      {showCancelConfirm ? (
        <ConfirmModal
          title="Cancel Booking"
          message="Do you want to cancel this booking?"
          confirmLabel={busyAction === "cancel" ? "Cancelling..." : "Yes"}
          busy={busyAction === "cancel"}
          onCancel={() => setShowCancelConfirm(false)}
          onConfirm={async () => {
            setShowCancelConfirm(false);
            await runAction("cancel");
          }}
          testId="admin-booking-cancel-confirm-modal"
        />
      ) : null}

      <section className="panel section-stack" data-testid="admin-booking-reviews-panel">
        <div className="section-heading">
          <h2>Booking Review</h2>
          <span>{bookingReviews.length} total</span>
        </div>
        {bookingReviews.length === 0 ? (
          <div className="empty-state">
            <strong>No reviews for this booking yet.</strong>
            <span>When a participant leaves a review, it will appear here.</span>
          </div>
        ) : (
          <div className="stack gap-md">
            {bookingReviews.map((review) => (
              <article className="review-card" key={review.id}>
                <div className="section-heading">
                  <strong>{review.offerTitle}</strong>
                  <div className="icon-action-group">
                    <Link
                      className="icon-action-link"
                      to={`/reviews/${review.publicId}/edit`}
                      aria-label="Edit review"
                      title="Edit review"
                    >
                      <EditIcon />
                    </Link>
                    <button
                      type="button"
                      className="icon-action-button"
                      aria-label="Delete review"
                      title="Delete review"
                      disabled={deletingReviewId === review.id}
                      onClick={() => setPendingDeleteReview(review)}
                    >
                      <DeleteIcon />
                    </button>
                  </div>
                </div>
                <span className="offer-review-author">{review.authorDisplayName}</span>
                <div className="offer-review-score-row">
                  <StarRating value={review.rating} />
                </div>
                <small className="muted">{formatReviewTimestamp(review.createdAt, review.updatedAt)}</small>
                <p>{review.comment}</p>
              </article>
            ))}
          </div>
        )}
      </section>

      {pendingDeleteReview ? (
        <ConfirmModal
          title="Delete Review"
          message="Do you want to delete this review?"
          confirmLabel={deletingReviewId === pendingDeleteReview.id ? "Deleting..." : "Yes"}
          busy={deletingReviewId === pendingDeleteReview.id}
          onCancel={() => setPendingDeleteReview(null)}
          onConfirm={async () => {
            if (!token) {
              return;
            }
            const review = pendingDeleteReview;
            setDeletingReviewId(review.id);
            try {
              await api.deleteReview(token, review.publicId);
              setPendingDeleteReview(null);
              setFeedback({ tone: "success", text: "Review deleted." });
              await load();
            } catch (err) {
              setFeedback({
                tone: "error",
                text: err instanceof ApiError ? err.message : "Failed to delete review"
              });
            } finally {
              setDeletingReviewId(null);
            }
          }}
          testId="admin-booking-review-delete-confirm-modal"
        />
      ) : null}

    </div>
  );
}
