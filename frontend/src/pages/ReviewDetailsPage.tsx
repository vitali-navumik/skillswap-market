import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { ConfirmModal } from "../components/ConfirmModal";
import { DeleteIcon, EditIcon } from "../components/ActionIcons";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import { formatRatingValue, formatReviewTimestamp } from "../lib/ratings";
import type { AuthUser, Review } from "../types";

interface ReviewDetailsPageProps {
  token: string | null;
  user: AuthUser | null;
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

export function ReviewDetailsPage({ token, user }: ReviewDetailsPageProps) {
  const navigate = useNavigate();
  const params = useParams();
  const reviewPublicId = params.reviewPublicId ?? "";
  const [review, setReview] = useState<Review | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!token || !reviewPublicId) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        setReview(await api.getReview(token, reviewPublicId));
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load review");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [reviewPublicId, token]);

  const targetLabel = review && review.authorId === review.targetUserId ? "Yourself" : review?.targetUserDisplayName;
  const canManageReview = useMemo(
    () => Boolean(user && review && (user.roles.includes("ADMIN") || user.id === review.authorId)),
    [review, user]
  );

  const onDelete = async () => {
    if (!token || !review) {
      return;
    }

    setDeleting(true);
    setError("");

    try {
      await api.deleteReview(token, review.publicId);
      navigate(review.bookingPublicId ? `/bookings/${review.bookingPublicId}` : "/bookings", {
        replace: true,
        state: { flash: "Review deleted." }
      });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to delete review");
      setDeleting(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/reviews/${reviewPublicId}` }} />;
  }

  if (loading) {
    return <div className="panel" data-testid="review-details-loading">Loading review details...</div>;
  }

  if (!review) {
    return <div className="panel" data-testid="review-details-not-found">Review not found.</div>;
  }

  if (!user.roles.includes("ADMIN") && review.bookingPublicId) {
    return <Navigate to={`/bookings/${review.bookingPublicId}`} replace />;
  }

  return (
    <div className="stack gap-xl detail-shell review-details-page" data-testid="review-details-page">
      <BackButton fallbackTo={review.bookingPublicId ? `/bookings/${review.bookingPublicId}` : "/bookings"} testId="review-back-link" />

      <section className="panel section-stack">
        <div className="section-heading">
          <div>
            <span className="eyebrow">{canManageReview ? "My Review" : "Review"}</span>
            <h1>{review.offerTitle}</h1>
          </div>
          <div className="icon-action-group">
            {canManageReview ? (
              <Link
                className="icon-action-link"
                data-testid="review-open-edit-page"
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
                data-testid="review-delete-button"
                disabled={deleting}
                onClick={() => setShowDeleteConfirm(true)}
                aria-label="Delete review"
                title="Delete review"
              >
                <DeleteIcon />
              </button>
            ) : null}
          </div>
        </div>
        <div className="stack gap-sm">
          <div className="offer-review-score-row">
            <StarRating value={review.rating} />
          </div>
          <div className="offer-meta">
            {!canManageReview ? <span>By {review.authorDisplayName}</span> : null}
            {targetLabel ? <span>{targetLabel}</span> : null}
            <span>{formatReviewTimestamp(review.createdAt, review.updatedAt)}</span>
          </div>
        </div>
        {error ? <div className="alert error" data-testid="review-details-error">{error}</div> : null}
      </section>

      <section className="panel section-stack review-content-panel">
        <div className="section-heading">
          <h2>Comment</h2>
        </div>
        <article className="admin-card">
          <p>{review.comment}</p>
        </article>
      </section>

      {showDeleteConfirm ? (
        <ConfirmModal
          title="Delete Review"
          message="Do you want to delete this review?"
          confirmLabel={deleting ? "Deleting..." : "Yes"}
          busy={deleting}
          onCancel={() => setShowDeleteConfirm(false)}
          onConfirm={async () => {
            setShowDeleteConfirm(false);
            await onDelete();
          }}
          testId="review-delete-confirm-modal"
        />
      ) : null}
    </div>
  );
}
