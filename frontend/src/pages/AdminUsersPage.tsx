import { useEffect, useMemo, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import { getFullName } from "../lib/userNames";
import type { AuthUser } from "../types";

interface AdminUsersPageProps {
  token: string | null;
  user: AuthUser | null;
}

const allUserStatuses: AuthUser["status"][] = ["ACTIVE", "INACTIVE"];

export function AdminUsersPage({ token, user }: AdminUsersPageProps) {
  const [users, setUsers] = useState<AuthUser[]>([]);
  const [userFilter, setUserFilter] = useState<number | "">("");
  const [statusFilter, setStatusFilter] = useState<AuthUser["status"] | "">("");
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
        setUsers(await api.listUsers(token));
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load users");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [token]);

  const statusOptions = useMemo(() => allUserStatuses, []);
  const userOptions = useMemo(
    () => users
      .map((entry) => ({
        id: entry.id,
        label: getFullName(entry.firstName, entry.lastName, entry.displayName)
      }))
      .sort((left, right) => left.label.localeCompare(right.label)),
    [users]
  );
  const visibleUsers = users.filter((entry) => {
    const userMatch = userFilter === "" || entry.id === userFilter;
    const statusMatch = statusFilter === "" || entry.status === statusFilter;
    return userMatch && statusMatch;
  });

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: "/users" }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-users-page-loading">Loading user workspace...</div>;
  }

  return (
    <div className="stack gap-xl list-page-shell" data-testid="admin-users-page">
      <div className="filter-layout">
        <aside className="panel filter-sidebar" data-testid="admin-users-filters-panel">
          <div className="stack gap-md">
            <div className="sidebar-heading">
              <strong>Filters</strong>
            </div>
            <label>
              User
              <select data-testid="admin-users-user-filter" value={userFilter} onChange={(event) => setUserFilter(event.target.value ? Number(event.target.value) : "")}>
                <option value="">All users</option>
                {userOptions.map((option) => (
                  <option key={option.id} value={option.id}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Status
              <select data-testid="admin-users-status-filter" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as AuthUser["status"] | "")}>
                <option value="">All statuses</option>
                {statusOptions.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </aside>

        <section className="panel section-stack filter-content">
          {error ? <div className="alert error" data-testid="admin-users-page-error">{error}</div> : null}
          {visibleUsers.length === 0 ? (
            <div className="empty-state" data-testid="admin-users-page-empty-state">
              <strong>No users found.</strong>
              <span>Try another user or status filter.</span>
            </div>
          ) : (
            <>
              <div className="inline-toolbar">
                <span className="list-total">{visibleUsers.length} total</span>
              </div>
              <div className="data-table" data-testid="admin-users-page-list">
                <div className="data-table-head admin-users-table">
                  <span>Full name</span>
                  <span>Email</span>
                  <span>Roles</span>
                  <span>Status</span>
                </div>
                {visibleUsers.map((entry) => (
                  <div className="data-table-row admin-users-table" key={entry.id} data-testid={`admin-user-list-item-${entry.id}`}>
                    <Link className="text-link" to={`/users/${entry.publicId}`}>
                      <strong>{getFullName(entry.firstName, entry.lastName, entry.displayName)}</strong>
                    </Link>
                    <span className="muted">{entry.email}</span>
                    <div className="pill-list">
                      {entry.roles.map((role) => (
                        <span className="badge subtle" key={role}>
                          {role}
                        </span>
                      ))}
                    </div>
                    <span className={`badge subtle status-chip status-${entry.status.toLowerCase()}`}>{entry.status}</span>
                  </div>
                ))}
              </div>
            </>
          )}
        </section>
      </div>
    </div>
  );
}
