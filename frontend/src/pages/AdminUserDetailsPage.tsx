import { useEffect, useState } from "react";
import { Link, Navigate, useLocation, useParams } from "react-router-dom";
import { EditIcon } from "../components/ActionIcons";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import { getFullName } from "../lib/userNames";
import type { AuthUser } from "../types";

interface AdminUserDetailsPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function AdminUserDetailsPage({ token, user }: AdminUserDetailsPageProps) {
  const params = useParams();
  const userPublicId = params.userPublicId ?? "";
  const location = useLocation();
  const locationState = (location.state as { flash?: string } | null) ?? null;
  const [entry, setEntry] = useState<AuthUser | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      if (!token || !userPublicId) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const userResult = await api.getUser(token, userPublicId);
        setEntry(userResult);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load user");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [token, userPublicId]);

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/users/${userPublicId}` }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-user-details-loading">Loading user details...</div>;
  }

  if (!entry) {
    return <div className="panel" data-testid="admin-user-details-not-found">User not found.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell" data-testid="admin-user-details-page">
      <BackButton fallbackTo="/users" testId="admin-user-back-link" />
      <section className="panel section-stack" data-testid="admin-user-summary">
        <div className="section-heading">
          <h1>{getFullName(entry.firstName, entry.lastName, entry.displayName)}</h1>
        </div>

        {locationState?.flash ? <div className="alert success" data-testid="admin-user-flash">{locationState.flash}</div> : null}
        {error ? <div className="alert error" data-testid="admin-user-details-error">{error}</div> : null}

        <div className="action-row">
          <Link
            className="icon-action-link"
            data-testid="admin-user-open-edit-page"
            to={`/users/${entry.publicId}/edit`}
            aria-label="Edit profile"
            title="Edit profile"
          >
            <EditIcon />
          </Link>
        </div>
      </section>

      <section className="panel section-stack" data-testid="admin-user-details-panel">
        <div className="section-heading">
          <h2>Profile Details</h2>
        </div>

        <div className="profile-table-wrap" data-testid="admin-user-details-table">
          <table className="profile-table">
            <thead>
              <tr>
                <th>First name</th>
                <th>Last name</th>
                <th>Email</th>
                <th>Roles</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td data-testid="admin-user-first-name-cell">{entry.firstName}</td>
                <td data-testid="admin-user-last-name-cell">{entry.lastName}</td>
                <td data-testid="admin-user-email-cell">{entry.email}</td>
                <td data-testid="admin-user-roles-cell">
                  <div className="pill-list">
                    {entry.roles.map((role) => (
                      <span className="badge subtle" key={role}>
                        {role}
                      </span>
                    ))}
                  </div>
                </td>
                <td data-testid="admin-user-status-cell">
                  <span className={`badge subtle status-chip status-${entry.status.toLowerCase()}`}>
                    {entry.status}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
