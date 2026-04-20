import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Offer } from "../types";

interface AdminOffersPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function AdminOffersPage({ token, user }: AdminOffersPageProps) {
  const [offers, setOffers] = useState<Offer[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      if (!token) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const response = await api.listManagedOffers(token);
        setOffers(response.content);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load offers");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [token]);

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: "/offers" }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-offers-page-loading">Loading offer workspace...</div>;
  }

  return (
    <div className="stack gap-xl" data-testid="admin-offers-page">
      <BackButton fallbackTo="/offers" testId="admin-offers-back-link" />
      <section className="panel section-stack">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Admin workspace</span>
            <h1>Offers</h1>
          </div>
          <span>{offers.length} total</span>
        </div>
        <p className="muted">
          Browse offers, open one offer record at a time, and edit only from a dedicated edit page.
        </p>
        {error ? <div className="alert error" data-testid="admin-offers-page-error">{error}</div> : null}
      </section>

      {offers.length === 0 ? (
        <section className="panel">
          <div className="empty-state" data-testid="admin-offers-page-empty-state">
            <strong>No offers found.</strong>
            <span>Offer records will appear here once they exist in the environment.</span>
          </div>
        </section>
      ) : (
        <section className="panel section-stack">
          <div className="section-heading">
            <h2>Offer list</h2>
            <span>Open a record to inspect details</span>
          </div>
          <div className="admin-grid" data-testid="admin-offers-page-list">
            {offers.map((offer) => (
              <article className="admin-card" key={offer.id} data-testid={`admin-offer-list-item-${offer.id}`}>
                <div className="section-heading">
                  <Link className="text-link" to={`/offers/${offer.publicId}`}>
                    <strong>{offer.title}</strong>
                  </Link>
                  <span className={`badge subtle status-chip status-${offer.status.toLowerCase()}`}>{offer.status}</span>
                </div>
                <div className="muted">{offer.mentorDisplayName}</div>
                <div className="offer-meta">
                  <span>{offer.category}</span>
                  <span>{offer.durationMinutes} min</span>
                  <span>{offer.priceCredits} credits</span>
                </div>
                <p>{offer.description}</p>
                <small className="muted">
                  Updated {new Date(offer.updatedAt).toLocaleString()}
                </small>
              </article>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
