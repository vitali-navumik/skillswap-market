import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Booking } from "../types";

interface CreateReviewPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function CreateReviewPage({ token, user }: CreateReviewPageProps) {
  const navigate = useNavigate();
  const params = useParams();
  const bookingPublicId = params.bookingPublicId ?? "";
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!token || !bookingPublicId) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        setBookings(await api.listBookings(token));
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load booking");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [bookingPublicId, token]);

  const booking = useMemo(() => bookings.find((entry) => entry.publicId === bookingPublicId) ?? null, [bookings, bookingPublicId]);
  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token || !booking || !comment.trim()) {
      return;
    }

    setSaving(true);
    setError("");

    try {
      const created = await api.createReview(token, booking.publicId, { rating, comment });
      navigate(`/bookings/${booking.publicId}`, { replace: true, state: { flash: "Review submitted." } });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to submit review");
      setSaving(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/bookings/${bookingPublicId}/review/new` }} />;
  }

  if (loading) {
    return <div className="panel" data-testid="review-create-loading">Loading review form...</div>;
  }

  if (!booking) {
    return <div className="panel" data-testid="review-create-not-found">Booking not found.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell review-edit-page" data-testid="review-create-page">
      <BackButton fallbackTo={`/bookings/${booking.publicId}`} testId="review-create-back-link" />
      <section className="panel section-stack review-summary-panel">
        <div className="section-heading">
          <div>
            <h1>{booking.offerTitle}</h1>
          </div>
        </div>
        <div className="offer-meta">
          <span>by {booking.mentorDisplayName}</span>
        </div>
      </section>

      <form className="panel stack gap-md review-edit-form" onSubmit={onSubmit}>
        <label className="review-edit-rating-field">
          Rating
          <select data-testid="review-create-rating" value={rating} onChange={(event) => setRating(Number(event.target.value))}>
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
            data-testid="review-create-comment"
            value={comment}
            onChange={(event) => setComment(event.target.value)}
            placeholder="What should others know about this session?"
          />
        </label>

        {error ? <div className="alert error" data-testid="review-create-error">{error}</div> : null}

        <div className="action-row">
          <button className="primary-button" type="submit" disabled={saving || !comment.trim()} data-testid="review-create-save">
            {saving ? "Submitting..." : "Submit review"}
          </button>
          <Link className="ghost-button" to={`/bookings/${booking.publicId}`}>
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
