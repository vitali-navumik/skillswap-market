import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { EditIcon } from "../components/ActionIcons";
import { api, ApiError } from "../lib/api";
import { formatRatingOutOfFive, formatRatingValue, formatReviewTimestamp } from "../lib/ratings";
import type { AuthUser, Offer, Review, Slot } from "../types";

interface OfferDetailsPageProps {
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

export function OfferDetailsPage({ token, user }: OfferDetailsPageProps) {
  const params = useParams();
  const offerPublicId = params.offerPublicId!;
  const [offer, setOffer] = useState<Offer | null>(null);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [bookingMessage, setBookingMessage] = useState("");
  const [bookingSlotId, setBookingSlotId] = useState<number | null>(null);
  const [slotStatusFilter, setSlotStatusFilter] = useState<"all" | Slot["status"]>("all");

  const load = async () => {
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
    void load();
  }, [offerPublicId]);

  const canEditOffer = Boolean(
    user &&
      offer &&
      (user.roles.includes("ADMIN") || (user.id === offer.mentorId && user.roles.includes("MENTOR")))
  );
  const canBook = Boolean(user && offer && user.roles.includes("STUDENT"));
  const showSlotActions = canBook;
  const averageRating = reviews.length
    ? reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length
    : null;
  const filteredSlots = slots
    .filter((slot) => new Date(slot.startTime).getTime() > Date.now())
    .filter((slot) => slotStatusFilter === "all" || slot.status === slotStatusFilter)
    .sort((left, right) => new Date(left.startTime).getTime() - new Date(right.startTime).getTime());

  const onBookSlot = async (slotId: number) => {
    if (!token) {
      setBookingMessage("Log in first to create a booking.");
      return;
    }

    setBookingSlotId(slotId);
    setBookingMessage("");

    try {
      if (!user) {
        throw new Error("User context is required to create booking");
      }
      await api.createBooking(token, { slotId, studentId: user.id });
      setBookingMessage("Booking created. Check My Bookings for the new reservation.");
      await load();
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setBookingMessage("This slot is no longer available. The list has been refreshed.");
        await load();
      } else {
        setBookingMessage(err instanceof ApiError ? err.message : "Failed to create booking");
      }
    } finally {
      setBookingSlotId(null);
    }
  };

  if (loading) {
    return <div className="panel" data-testid="offer-details-loading">Loading offer details...</div>;
  }

  if (!offer) {
    return <div className="panel" data-testid="offer-details-not-found">Offer not found.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell offer-details-page" data-testid="offer-details-page">
      {error ? <div className="alert error" data-testid="offer-details-error">{error}</div> : null}

      <section className="offer-hero" data-testid={`offer-details-${offer.id}`}>
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

        {canEditOffer ? (
          <div className="action-row offer-hero-actions">
            <Link
              className="icon-action-link"
              data-testid="offer-open-edit-page"
              to={`/offers/${offer.publicId}/edit`}
              aria-label="Edit offer"
              title="Edit offer"
            >
              <EditIcon />
            </Link>
          </div>
        ) : null}
      </section>

      <section className="panel" data-testid="offer-slots-panel">
        <div className="section-heading">
          <h2>Slots</h2>
          <span>{filteredSlots.length} shown</span>
        </div>

        <div className="pill-list" data-testid="offer-slots-filters">
          {([
            { id: "all", label: "All" },
            { id: "OPEN", label: "Available" },
            { id: "BOOKED", label: "Booked" }
          ] as const).map((filter) => (
            <button
              key={filter.id}
              type="button"
              className={`filter-chip ${slotStatusFilter === filter.id ? "selected" : ""}`}
              data-testid={`offer-slots-filter-${filter.id.toLowerCase()}`}
              onClick={() => setSlotStatusFilter(filter.id)}
            >
              {filter.label}
            </button>
          ))}
        </div>

        {bookingMessage ? (
          <div
            className={`alert ${bookingMessage.startsWith("Booking created") ? "success" : "error"}`}
            data-testid="offer-booking-feedback"
          >
            {bookingMessage}
          </div>
        ) : null}

        {filteredSlots.length === 0 ? (
          <p className="muted" data-testid="offer-slots-empty-state">No upcoming slots.</p>
        ) : (
          <div className="slots-table-wrap" data-testid="offer-slots-list">
            <table className="slots-table" data-testid="offer-slots-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                  {showSlotActions ? <th>Action</th> : null}
                </tr>
              </thead>
              <tbody>
                {filteredSlots.map((slot) => (
                  <tr key={slot.id} data-testid={`offer-slot-${slot.id}`}>
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
                    {showSlotActions ? (
                      <td>
                        {slot.status === "OPEN" ? (
                        <button
                          className="primary-button"
                          data-testid={`offer-book-slot-${slot.id}`}
                          disabled={bookingSlotId === slot.id}
                          onClick={() => onBookSlot(slot.id)}
                        >
                          {bookingSlotId === slot.id ? "Booking..." : "Book"}
                        </button>
                        ) : (
                          <span className="muted">-</span>
                        )}
                      </td>
                    ) : null}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!user ? (
          <p className="muted">
            Want to reserve a slot? <Link to="/login">Log in</Link> first.
          </p>
        ) : null}
      </section>

      <section className="panel" data-testid="offer-reviews-panel">
        <div className="section-heading">
          <h2>Reviews</h2>
          <div className="offer-reviews-meta">
            <span>{reviews.length} total</span>
            {averageRating !== null ? (
              <span className="offer-reviews-average">
                <StarRating value={averageRating} />
                <span>{formatRatingOutOfFive(averageRating)}</span>
              </span>
            ) : null}
          </div>
        </div>

        {reviews.length === 0 ? (
          <div className="empty-state" data-testid="offer-reviews-empty-state">
            <strong>No reviews yet for this offer.</strong>
            <span>Once a completed session is reviewed, the rating summary will show up here.</span>
          </div>
        ) : (
          <div className="review-list" data-testid="offer-reviews-list">
            {reviews.map((review) => (
              <article className="review-card" key={review.id} data-testid={`offer-review-${review.id}`}>
                <span className="offer-review-author">{review.authorDisplayName}</span>
                <div className="offer-review-score-row">
                  <StarRating value={review.rating} />
                </div>
                <small className="muted">
                  {formatReviewTimestamp(review.createdAt, review.updatedAt)}
                </small>
                <p>{review.comment}</p>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
