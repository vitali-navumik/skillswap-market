import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Offer } from "../types";

interface CreateOfferPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function CreateOfferPage({ token, user }: CreateOfferPageProps) {
  const navigate = useNavigate();
  const projectCategories = ["Career", "Databases", "Frontend", "Operations"] as const;
  const statusOptions: Offer["status"][] = ["DRAFT", "ACTIVE", "ARCHIVED", "BLOCKED"];
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [status, setStatus] = useState<Offer["status"]>("DRAFT");
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [priceCredits, setPriceCredits] = useState(20);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const canMentor = Boolean(user?.roles.includes("MENTOR") && !user?.roles.includes("ADMIN"));

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token) {
      setError("You need to log in first.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const offer = await api.createOffer(token, {
        title,
        description,
        category,
        durationMinutes,
        priceCredits,
        status
      });
      navigate(`/offers/${offer.publicId}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create offer");
    } finally {
      setLoading(false);
    }
  };

  if (!canMentor) {
    return <div className="panel">This page is available only for mentor accounts.</div>;
  }

  return (
    <section className="stack gap-lg detail-shell offer-editor-page" data-testid="create-offer-page">
      <form className="panel stack gap-md offer-editor-form" onSubmit={onSubmit} data-testid="create-offer-form">
        <div className="section-heading">
          <h1>Create offer</h1>
        </div>

        <label>
          Title
          <input
            data-testid="create-offer-title-input"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            required
          />
        </label>

        <label>
          Description
          <textarea
            data-testid="create-offer-description-input"
            rows={5}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            required
          />
        </label>

        <div className="offer-editor-four-col">
          <label className="offer-editor-select-field offer-editor-select-field-category">
            Category
            <select
              data-testid="create-offer-category-select"
              value={category}
              onChange={(event) => setCategory(event.target.value)}
              required
            >
              <option value="">Choose category</option>
              {projectCategories.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
          <label className="offer-editor-select-field offer-editor-select-field-status">
            Status
            <select
              data-testid="create-offer-status-select"
              value={status}
              onChange={(event) => setStatus(event.target.value as Offer["status"])}
              required
            >
              {statusOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>
          <label className="offer-editor-number-field offer-editor-number-field-duration">
            Duration (minutes)
            <input
              data-testid="create-offer-duration-input"
              type="number"
              min={1}
              value={durationMinutes}
              onChange={(event) => setDurationMinutes(Number(event.target.value))}
                required
              />
          </label>
          <label className="offer-editor-number-field offer-editor-number-field-price">
            Price (credits)
            <input
              data-testid="create-offer-price-input"
              type="number"
              min={1}
              value={priceCredits}
              onChange={(event) => setPriceCredits(Number(event.target.value))}
                required
              />
          </label>
        </div>

        {error ? <div className="alert error" data-testid="create-offer-feedback">{error}</div> : null}

        <div className="action-row">
          <button className="primary-button offer-editor-submit" type="submit" disabled={loading} data-testid="create-offer-submit">
            {loading ? "Saving..." : "Create offer"}
          </button>
        </div>
      </form>
    </section>
  );
}
