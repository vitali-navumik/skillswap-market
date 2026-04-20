import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import { DeleteIcon, EditIcon } from "../components/ActionIcons";
import { BackButton } from "../components/BackButton";
import { ConfirmModal } from "../components/ConfirmModal";
import { api, ApiError } from "../lib/api";
import { formatRatingValue, formatReviewTimestamp } from "../lib/ratings";
import type { AuthUser, Offer, Review, Slot } from "../types";

interface AdminOfferDetailsPageProps {
  token: string | null;
  user: AuthUser | null;
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

export function AdminOfferDetailsPage({ token, user }: AdminOfferDetailsPageProps) {
  const params = useParams();
  const offerPublicId = params.offerPublicId ?? "";
  const [offer, setOffer] = useState<Offer | null>(null);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deletingReviewId, setDeletingReviewId] = useState<number | null>(null);
  const [pendingDeleteReview, setPendingDeleteReview] = useState<Review | null>(null);

  const loadOfferDetails = async () => {
    if (!token || !offerPublicId) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const [offerResult, slotResult, reviewResult] = await Promise.all([
        api.getOffer(offerPublicId),
        api.getSlots(offerPublicId),
        api.getOfferReviews(offerPublicId)
      ]);
      setOffer(offerResult);
      setSlots(slotResult);
      setReviews(reviewResult);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load offer");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOfferDetails();
  }, [offerPublicId, token]);

  const sortedSlots = useMemo(
    () =>
      [...slots].sort(
        (left, right) => new Date(right.startTime).getTime() - new Date(left.startTime).getTime()
      ),
    [slots]
  );

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/offers/${offerPublicId}` }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-offer-details-loading">Loading offer details...</div>;
  }

  if (!offer) {
    return <div className="panel" data-testid="admin-offer-details-not-found">Offer not found.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell" data-testid="admin-offer-details-page">
      <BackButton fallbackTo="/offers" testId="admin-offer-back-link" />
      <section className="offer-hero" data-testid={`admin-offer-details-${offer.id}`}>
        <div className="stack gap-sm">
          <span className="badge">{offer.category}</span>
          <h1>{offer.title}</h1>
          <p>{offer.description}</p>
          <div className="offer-meta">
            <span>{offer.durationMinutes} min</span>
            <span>{offer.priceCredits} credits</span>
            <span>by {offer.mentorDisplayName}</span>
            <span>Status: {offer.status}</span>
          </div>
        </div>

        <div className="action-row offer-hero-actions">
          <Link
            className="icon-action-link"
            data-testid="admin-offer-open-edit-page"
            to={`/offers/${offer.publicId}/edit`}
            aria-label="Edit offer"
            title="Edit offer"
          >
            <EditIcon />
          </Link>
        </div>
      </section>

      {error ? <div className="alert error" data-testid="admin-offer-details-error">{error}</div> : null}

      <section className="panel section-stack" data-testid="admin-offer-slots-panel">
        <div className="section-heading">
          <h2>Slots</h2>
          <span>{slots.length} slot(s)</span>
        </div>

        {sortedSlots.length === 0 ? (
          <p className="muted">No slots yet.</p>
        ) : (
          <div className="bookings-table-wrap" data-testid="admin-offer-slots-table-wrap">
            <table className="bookings-table" data-testid="admin-offer-slots-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {sortedSlots.map((slot) => (
                  <tr key={slot.id}>
                    <td>{new Date(slot.startTime).toLocaleDateString()}</td>
                    <td>
                      {new Date(slot.startTime).toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit"
                      })}{" "}
                      -{" "}
                      {new Date(slot.endTime).toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit"
                      })}
                    </td>
                    <td>
                      <span className="badge subtle">{slot.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel section-stack" data-testid="admin-offer-reviews-panel">
        <div className="inline-toolbar">
          <span className="list-total">{reviews.length} total</span>
        </div>
        {reviews.length === 0 ? (
          <p className="muted">No reviews yet.</p>
        ) : (
          <div className="admin-grid">
            {reviews.map((review) => (
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
              await loadOfferDetails();
            } catch (err) {
              setError(err instanceof ApiError ? err.message : "Failed to delete review");
            } finally {
              setDeletingReviewId(null);
            }
          }}
          testId="admin-offer-review-delete-confirm-modal"
        />
      ) : null}
    </div>
  );
}
