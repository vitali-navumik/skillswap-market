import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Offer, OfferStatus } from "../types";

type CatalogSort = "newest" | "priceAsc" | "priceDesc" | "titleAsc" | "titleDesc";

const sortLabels: Record<CatalogSort, string> = {
  newest: "Newest",
  priceAsc: "Price: low to high",
  priceDesc: "Price: high to low",
  titleAsc: "Title: A to Z",
  titleDesc: "Title: Z to A"
};

interface CatalogPageProps {
  token?: string | null;
  user?: AuthUser | null;
  mode?: "catalog" | "myOffers";
}

function sortOffers(offers: Offer[], sort: CatalogSort) {
  const sorted = [...offers];
  switch (sort) {
    case "priceAsc":
      sorted.sort((left, right) => left.priceCredits - right.priceCredits);
      break;
    case "priceDesc":
      sorted.sort((left, right) => right.priceCredits - left.priceCredits);
      break;
    case "titleAsc":
      sorted.sort((left, right) => left.title.localeCompare(right.title));
      break;
    case "titleDesc":
      sorted.sort((left, right) => right.title.localeCompare(left.title));
      break;
    case "newest":
    default:
      sorted.sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime());
      break;
  }
  return sorted;
}

export function CatalogPage({ token = null, user = null, mode = "catalog" }: CatalogPageProps) {
  const [offers, setOffers] = useState<Offer[]>([]);
  const [categoryOptions, setCategoryOptions] = useState<string[]>([]);
  const [mentorOptions, setMentorOptions] = useState<{ id: number; label: string }[]>([]);
  const [statusOptions, setStatusOptions] = useState<OfferStatus[]>([]);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const [mentorId, setMentorId] = useState<number | "">("");
  const [status, setStatus] = useState<OfferStatus | "">("");
  const [sort, setSort] = useState<CatalogSort>("newest");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const isAdmin = Boolean(user?.roles.includes("ADMIN") && token);
  const isMyOffersMode = mode === "myOffers";
  const showMentorFilter = !isMyOffersMode;
  const showStatusFilter = isAdmin || isMyOffersMode;

  const popularSearches = ["SQL", "React", "Debugging", "Interview"];
  const hasActiveFilters = Boolean(search || category || mentorId !== "" || status || sort !== "newest");

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setSearch(searchInput.trim());
    }, 350);

    return () => window.clearTimeout(timeoutId);
  }, [searchInput]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");

    const load = async () => {
      try {
        const loadedOffers = isMyOffersMode && token
          ? (await api.listOwnOffers(token)).content
          : isAdmin && token
            ? (await api.listManagedOffers(token)).content
            : (await api.getOffers()).content;

        if (cancelled) {
          return;
        }

        setCategoryOptions(Array.from(new Set(loadedOffers.map((offer) => offer.category))).sort());
        setMentorOptions(
          Array.from(new Map(loadedOffers.map((offer) => [offer.mentorId, offer.mentorDisplayName])).entries())
            .map(([id, label]) => ({ id, label }))
            .sort((left, right) => left.label.localeCompare(right.label))
        );
        setStatusOptions(Array.from(new Set(loadedOffers.map((offer) => offer.status))).sort());

        const filteredOffers = loadedOffers.filter((offer) => {
          const searchMatch = !search || `${offer.title} ${offer.description}`.toLowerCase().includes(search.toLowerCase());
          const categoryMatch = !category || offer.category === category;
          const mentorMatch = mentorId === "" || offer.mentorId === mentorId;
          const statusMatch = status === "" || offer.status === status;
          return searchMatch && categoryMatch && mentorMatch && statusMatch;
        });

        setOffers(sortOffers(filteredOffers, sort));
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof ApiError ? err.message : "Failed to load offers");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void load();

    return () => {
      cancelled = true;
    };
  }, [category, isAdmin, isMyOffersMode, mentorId, search, sort, status, token]);

  const resetAll = () => {
    setSearchInput("");
    setSearch("");
    setCategory("");
    setMentorId("");
    setStatus("");
    setSort("newest");
  };

  return (
    <div className="stack gap-lg catalog-page list-page-shell">
      <div className="page-heading">
        <h1 data-testid="catalog-page-title">{isMyOffersMode ? "My Offers" : "Catalog"}</h1>
      </div>

      <div className="filter-layout">
        <aside className="panel filter-sidebar catalog-sidebar" data-testid="catalog-filters-panel">
          <div className="stack gap-sm catalog-filter-form" data-testid="catalog-search-panel">
            <div className="sidebar-heading">
              <strong>Filters</strong>
            </div>

            <div className="catalog-toolbar-grid">
              <label className="catalog-toolbar-field catalog-search-field" htmlFor="search">
                Keyword
                <input
                  id="search"
                  data-testid="catalog-search-input"
                  placeholder="Search"
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                />
              </label>

              <label className="catalog-toolbar-field">
                Category
                <select data-testid="catalog-category-select" value={category} onChange={(event) => setCategory(event.target.value)}>
                  <option value="">All categories</option>
                  {categoryOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
              </label>

              {showMentorFilter ? (
                <label className="catalog-toolbar-field">
                  Mentor
                  <select
                    data-testid="catalog-mentor-select"
                    value={mentorId}
                    onChange={(event) => setMentorId(event.target.value ? Number(event.target.value) : "")}
                  >
                    <option value="">All mentors</option>
                    {mentorOptions.map((option) => (
                      <option key={option.id} value={option.id}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>
              ) : null}

              {showStatusFilter ? (
                <label className="catalog-toolbar-field">
                  Status
                  <select data-testid="catalog-status-select" value={status} onChange={(event) => setStatus(event.target.value as OfferStatus | "")}>
                    <option value="">All statuses</option>
                    {statusOptions.map((option) => (
                      <option key={option} value={option}>
                        {option}
                      </option>
                    ))}
                  </select>
                </label>
              ) : null}

              <label className="catalog-toolbar-field">
                Sort
                <select data-testid="catalog-sort-select" value={sort} onChange={(event) => setSort(event.target.value as CatalogSort)}>
                  <option value="newest">{sortLabels.newest}</option>
                  <option value="priceAsc">{sortLabels.priceAsc}</option>
                  <option value="priceDesc">{sortLabels.priceDesc}</option>
                  <option value="titleAsc">{sortLabels.titleAsc}</option>
                  <option value="titleDesc">{sortLabels.titleDesc}</option>
                </select>
              </label>
            </div>

            <div className="catalog-sidebar-group">
              <span className="inline-note">Popular</span>
              <div className="pill-list">
                {popularSearches.map((item) => (
                  <button
                    key={item}
                    type="button"
                    className={`filter-chip ${search === item ? "selected" : ""}`}
                    data-testid={`catalog-popular-search-${item.toLowerCase()}`}
                    onClick={() => {
                      setSearchInput(item);
                      setSearch(item);
                    }}
                  >
                    {item}
                  </button>
                ))}
              </div>
            </div>

            {hasActiveFilters ? (
              <button type="button" className="text-link" data-testid="catalog-reset-filters" onClick={resetAll}>
                Clear all
              </button>
            ) : null}
          </div>
        </aside>

        <div className="filter-content">
          {error ? <div className="alert error" data-testid="catalog-error">{error}</div> : null}

          {loading ? (
            <div className="panel" data-testid="catalog-loading">Loading offers...</div>
          ) : offers.length === 0 ? (
            <div className="panel empty-state" data-testid="catalog-empty-state">
              <strong>No offers matched the current filters.</strong>
              <span>Try another keyword, change category, or reset filters to return to all visible offers.</span>
            </div>
          ) : (
            <>
              {(search || category || mentorId !== "" || status) && (
                <div className="catalog-results-bar" data-testid="catalog-results-summary">
                  <div className="pill-list catalog-active-filters">
                    {search ? <span className="badge subtle">Search: {search}</span> : null}
                    {category ? <span className="badge subtle">Category: {category}</span> : null}
                    {showMentorFilter && mentorId !== "" ? (
                      <span className="badge subtle">
                        Mentor: {mentorOptions.find((option) => option.id === mentorId)?.label ?? mentorId}
                      </span>
                    ) : null}
                    {status ? <span className="badge subtle">Status: {status}</span> : null}
                  </div>
                </div>
              )}

              <section className="offer-grid" data-testid="catalog-results-grid">
                {offers.map((offer) => (
                  <article className="offer-card" key={offer.id} data-testid={`offer-card-${offer.id}`}>
                    <div className="offer-card-header">
                      <span className="badge">{offer.category}</span>
                      <div className="pill-list">
                        {showStatusFilter ? (
                          <span className={`badge subtle status-chip status-${offer.status.toLowerCase()}`}>
                            {offer.status}
                          </span>
                        ) : null}
                        <span className="price">{offer.priceCredits} cr</span>
                      </div>
                    </div>
                    <h3>
                      <Link className="text-link" data-testid={`offer-open-details-${offer.id}`} to={`/offers/${offer.publicId}`}>
                        {offer.title}
                      </Link>
                    </h3>
                    <p>{offer.description}</p>
                    <div className="offer-card-footer">
                      <div className="offer-meta">
                        <span>{offer.durationMinutes} min</span>
                        <span>by {offer.mentorDisplayName}</span>
                      </div>
                    </div>
                  </article>
                ))}
              </section>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
