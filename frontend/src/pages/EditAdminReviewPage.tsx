import { FormEvent, useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Review } from "../types";

interface EditAdminReviewPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function EditAdminReviewPage({ token, user }: EditAdminReviewPageProps) {
  const navigate = useNavigate();
  const params = useParams();
  const reviewPublicId = params.reviewPublicId ?? "";
  const [review, setReview] = useState<Review | null>(null);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!token || !reviewPublicId) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const result = await api.getReview(token, reviewPublicId);
        setReview(result);
        setRating(result.rating);
        setComment(result.comment);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load review");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [reviewPublicId, token]);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token || !review || !comment.trim()) {
      return;
    }

    setSaving(true);
    setError("");

    try {
      await api.updateReview(token, review.publicId, {
        rating,
        comment
      });
      navigate("/reviews", { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to update review");
      setSaving(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/reviews/${reviewPublicId}/edit` }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-review-edit-loading">Loading review editor...</div>;
  }

  if (!review) {
    return <div className="panel" data-testid="admin-review-edit-not-found">Review not found.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell review-edit-page" data-testid="admin-review-edit-page">
      <BackButton fallbackTo="/reviews" testId="admin-review-edit-back-link" />
      <section className="panel section-stack review-summary-panel">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Edit review</span>
            <h1>{review.offerTitle}</h1>
          </div>
        </div>
      </section>

      <form className="panel stack gap-md review-edit-form" onSubmit={onSubmit}>

        <label className="review-edit-rating-field">
          Rating
          <select data-testid="admin-review-edit-rating" value={rating} onChange={(event) => setRating(Number(event.target.value))}>
            {[5, 4, 3, 2, 1].map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>

        <label>
          Comment
          <textarea
            rows={5}
            data-testid="admin-review-edit-comment"
            value={comment}
            onChange={(event) => setComment(event.target.value)}
          />
        </label>

        {error ? <div className="alert error" data-testid="admin-review-edit-error">{error}</div> : null}

        <div className="action-row">
          <button className="primary-button" type="submit" disabled={saving || !comment.trim()} data-testid="admin-review-edit-save">
            {saving ? "Saving..." : "Save review"}
          </button>
          <Link className="ghost-button" to="/reviews">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
