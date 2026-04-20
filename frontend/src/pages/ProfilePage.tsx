import { Link, Navigate, useLocation, useParams } from "react-router-dom";
import type { AuthUser } from "../types";
import { EditIcon } from "../components/ActionIcons";
import { getFullName } from "../lib/userNames";

interface ProfilePageProps {
  token: string | null;
  user: AuthUser | null;
}

export function ProfilePage({ token, user }: ProfilePageProps) {
  const params = useParams<{ userPublicId: string }>();
  const userPublicId = params.userPublicId ?? "";
  const location = useLocation();
  const locationState = (location.state as { flash?: string } | null) ?? null;

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/users/${userPublicId}` }} />;
  }

  if (!userPublicId || userPublicId !== user.publicId) {
    return <div className="panel">This profile page is available only for the signed-in user.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell" data-testid="profile-page">
      <section className="panel section-stack" data-testid="profile-summary">
        <div className="section-heading">
          <h1>{getFullName(user.firstName, user.lastName, user.displayName)}</h1>
        </div>

        {locationState?.flash ? <div className="alert success" data-testid="profile-flash">{locationState.flash}</div> : null}

        <div className="action-row">
          <Link
            className="icon-action-link"
            data-testid="profile-open-edit-page"
            to={`/users/${user.publicId}/edit`}
            aria-label="Edit profile"
            title="Edit profile"
          >
            <EditIcon />
          </Link>
        </div>
      </section>

      <section className="panel section-stack" data-testid="profile-details-panel">
        <div className="section-heading">
          <h2>Profile details</h2>
        </div>

        <div className="profile-table-wrap" data-testid="profile-details-table">
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
                <td data-testid="profile-first-name-cell">{user.firstName}</td>
                <td data-testid="profile-last-name-cell">{user.lastName}</td>
                <td data-testid="profile-email-cell">{user.email}</td>
                <td data-testid="profile-roles-cell">
                  <div className="pill-list">
                    {user.roles.map((role) => (
                      <span className="badge subtle" key={role}>
                        {role}
                      </span>
                    ))}
                  </div>
                </td>
                <td data-testid="profile-status-cell">
                  <span className={`badge subtle status-chip status-${user.status.toLowerCase()}`}>
                    {user.status}
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
