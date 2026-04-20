import { useEffect, useMemo, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { ConfirmModal } from "../components/ConfirmModal";
import { DeleteIcon, EditIcon } from "../components/ActionIcons";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import { formatRatingValue, formatReviewTimestamp } from "../lib/ratings";
import type { AuthUser, Review } from "../types";

interface AdminReviewsPageProps {
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

export function AdminReviewsPage({ token, user }: AdminReviewsPageProps) {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [users, setUsers] = useState<AuthUser[]>([]);
  const [reviewerFilter, setReviewerFilter] = useState<number | "">("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deletingReviewId, setDeletingReviewId] = useState<number | null>(null);
  const [pendingDeleteReview, setPendingDeleteReview] = useState<Review | null>(null);

  const loadReviews = async () => {
    if (!token) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const [reviewsResult, usersResult] = await Promise.all([
        api.listReviews(token),
        api.listUsers(token)
      ]);
      setReviews(reviewsResult);
      setUsers(usersResult);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load reviews");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadReviews();
  }, [token]);

  const reviewerOptions = useMemo(
    () => users
      .filter((entry) => entry.roles.includes("STUDENT"))
      .map((entry) => ({ id: entry.id, label: `${entry.firstName} ${entry.lastName}` }))
      .sort((left, right) => left.label.localeCompare(right.label)),
    [users]
  );
  const visibleReviews = reviews.filter((review) => reviewerFilter === "" || review.authorId === reviewerFilter);

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: "/reviews" }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-reviews-page-loading">Loading review workspace...</div>;
  }

  return (
    <div className="stack gap-xl list-page-shell" data-testid="admin-reviews-page">
      <BackButton fallbackTo="/offers" testId="admin-reviews-back-link" />

      <div className="filter-layout">
        <aside className="panel filter-sidebar" data-testid="admin-reviews-filters-panel">
          <div className="stack gap-md">
            <div className="sidebar-heading">
              <strong>Filters</strong>
            </div>
            <label>
              Reviewer
              <select data-testid="admin-reviews-reviewer-filter" value={reviewerFilter} onChange={(event) => setReviewerFilter(event.target.value ? Number(event.target.value) : "")}>
                <option value="">All</option>
                {reviewerOptions.map((option) => (
                  <option key={option.id} value={option.id}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </aside>

        <section className="panel section-stack filter-content">
          {error ? <div className="alert error" data-testid="admin-reviews-page-error">{error}</div> : null}
          {visibleReviews.length === 0 ? (
            <div className="empty-state" data-testid="admin-reviews-page-empty-state">
              <strong>No reviews found.</strong>
              <span>Try another reviewer filter.</span>
            </div>
          ) : (
            <>
              <div className="inline-toolbar">
                <span className="list-total">{visibleReviews.length} total</span>
              </div>
              <div className="admin-grid" data-testid="admin-reviews-page-list">
                {visibleReviews.map((review) => (
                  <article className="admin-card" key={review.id} data-testid={`admin-review-list-item-${review.id}`}>
                    <div className="section-heading">
                      <strong>{review.offerTitle}</strong>
                      <div className="icon-action-group">
                        <Link
                          className="icon-action-link"
                          to={`/reviews/${review.publicId}/edit`}
                          aria-label="Edit review"
                          title="Edit review"
                          data-testid={`admin-review-edit-${review.id}`}
                        >
                          <EditIcon />
                        </Link>
                        <button
                          type="button"
                          className="icon-action-button"
                          aria-label="Delete review"
                          title="Delete review"
                          data-testid={`admin-review-delete-${review.id}`}
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
            </>
          )}
        </section>
      </div>
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
              await loadReviews();
            } catch (err) {
              setError(err instanceof ApiError ? err.message : "Failed to delete review");
            } finally {
              setDeletingReviewId(null);
            }
          }}
          testId="admin-review-delete-confirm-modal"
        />
      ) : null}
    </div>
  );
}
