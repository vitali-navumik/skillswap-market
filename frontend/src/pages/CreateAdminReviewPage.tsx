import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import { getFullName } from "../lib/userNames";
import type { AuthUser, Booking, Offer } from "../types";

interface CreateAdminReviewPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function CreateAdminReviewPage({ token, user }: CreateAdminReviewPageProps) {
  const navigate = useNavigate();
  const [offers, setOffers] = useState<Offer[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [users, setUsers] = useState<AuthUser[]>([]);
  const [offerId, setOfferId] = useState<number | "">("");
  const [bookingId, setBookingId] = useState<number | "">("");
  const [targetUserId, setTargetUserId] = useState<number | "">("");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!token) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const [offersResult, bookingsResult, usersResult] = await Promise.all([
          api.listManagedOffers(token),
          api.listBookings(token),
          api.listUsers(token)
        ]);
        setOffers(offersResult.content);
        setBookings(bookingsResult);
        setUsers(usersResult);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load review create form");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [token]);

  const eligibleBookings = useMemo(
    () => (offerId === "" ? [] : bookings.filter((booking) => booking.offerId === Number(offerId))),
    [bookings, offerId]
  );

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token || offerId === "" || !comment.trim()) {
      return;
    }

    setSaving(true);
    setError("");

    try {
      const created = await api.createDirectReview(token, {
        offerId: Number(offerId),
        bookingId: bookingId === "" ? null : Number(bookingId),
        targetUserId: targetUserId === "" ? null : Number(targetUserId),
        rating,
        comment
      });
      navigate(`/reviews/${created.publicId}`, { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create admin review");
      setSaving(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: "/reviews/new" }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-review-create-loading">Loading review create page...</div>;
  }

  return (
    <div className="stack gap-xl" data-testid="admin-review-create-page">
      <BackButton fallbackTo="/reviews" testId="admin-review-create-back-link" />
      <section className="panel section-stack">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Admin review create</span>
            <h1>Create review</h1>
          </div>
        </div>
        <p className="muted">
          Create an admin review from a dedicated create page. The booking link is optional.
        </p>
      </section>

      <form className="panel stack gap-md" onSubmit={onSubmit}>
        <div className="section-heading">
          <h2>Review fields</h2>
          <span>Save only after all fields look correct.</span>
        </div>

        <div className="two-col">
          <label>
            Offer
            <select data-testid="admin-review-create-offer" value={offerId} onChange={(event) => setOfferId(event.target.value ? Number(event.target.value) : "")}>
              <option value="">Select offer</option>
              {offers.map((offer) => (
                <option key={offer.id} value={offer.id}>
                  {offer.title}
                </option>
              ))}
            </select>
          </label>
          <label>
            Booking (optional)
            <select data-testid="admin-review-create-booking" value={bookingId} onChange={(event) => setBookingId(event.target.value ? Number(event.target.value) : "")}>
              <option value="">No booking</option>
              {eligibleBookings.map((booking) => (
                <option key={booking.id} value={booking.id}>
                  #{booking.id} - {booking.studentDisplayName} to {booking.mentorDisplayName}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="two-col">
          <label>
            Target user (optional)
            <select data-testid="admin-review-create-target-user" value={targetUserId} onChange={(event) => setTargetUserId(event.target.value ? Number(event.target.value) : "")}>
              <option value="">Default offer mentor</option>
              {users.map((entry) => (
                <option key={entry.id} value={entry.id}>
                  {getFullName(entry.firstName, entry.lastName, entry.displayName)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Rating
            <select data-testid="admin-review-create-rating" value={rating} onChange={(event) => setRating(Number(event.target.value))}>
              {[5, 4, 3, 2, 1].map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>
        </div>

        <label>
          Comment
          <textarea
            rows={5}
            data-testid="admin-review-create-comment"
            value={comment}
            onChange={(event) => setComment(event.target.value)}
          />
        </label>

        {error ? <div className="alert error" data-testid="admin-review-create-error">{error}</div> : null}

        <div className="action-row">
          <button className="primary-button" type="submit" disabled={saving || offerId === "" || !comment.trim()} data-testid="admin-review-create-submit">
            {saving ? "Creating..." : "Create review"}
          </button>
          <Link className="ghost-button" to="/reviews">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
