import { FormEvent, useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Role } from "../types";

interface EditAdminUserPageProps {
  token: string | null;
  user: AuthUser | null;
  onUserUpdated?: (user: AuthUser) => void;
}

export function EditAdminUserPage({ token, user, onUserUpdated }: EditAdminUserPageProps) {
  const navigate = useNavigate();
  const params = useParams();
  const userPublicId = params.userPublicId ?? "";
  const [entry, setEntry] = useState<AuthUser | null>(null);
  const [email, setEmail] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [role, setRole] = useState<Role | "">("");
  const [status, setStatus] = useState<AuthUser["status"]>("ACTIVE");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!token || !userPublicId) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const result = await api.getUser(token, userPublicId);
        setEntry(result);
        setEmail(result.email);
        setFirstName(result.firstName);
        setLastName(result.lastName);
        setRole(result.roles[0] ?? "");
        setStatus(result.status);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load user");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [token, userPublicId]);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token || !entry) {
      return;
    }

    setSaving(true);
    setError("");
    if (password || confirmPassword) {
      if (!password || !confirmPassword) {
        setError("Fill in both password fields to change the password.");
        setSaving(false);
        return;
      }
      if (password !== confirmPassword) {
        setError("Password and confirm password must match.");
        setSaving(false);
        return;
      }
    }

    try {
      const updatedUser = await api.updateUser(token, entry.publicId, {
        email,
        firstName,
        lastName,
        password: password || undefined,
        roles: role ? [role] : [],
        status
      });
      if (user && updatedUser.id === user.id) {
        onUserUpdated?.(updatedUser);
      }
      navigate(`/users/${entry.publicId}`, { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to update user");
      setSaving(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/users/${userPublicId}/edit` }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-user-edit-loading">Loading user editor...</div>;
  }

  if (!entry) {
    return <div className="panel" data-testid="admin-user-edit-not-found">User not found.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell" data-testid="admin-user-edit-page">
      <BackButton fallbackTo={`/users/${entry.publicId}`} testId="admin-user-edit-back-link" />
      <form className="panel stack gap-md register-form" onSubmit={onSubmit}>
        <div className="section-heading">
          <h1>Edit profile</h1>
        </div>

        <div className="stack gap-md">
          <div className="two-col register-form-row">
            <label className="register-account-field">
            First name
            <input value={firstName} onChange={(event) => setFirstName(event.target.value)} required />
          </label>
            <label className="register-account-field">
            Last name
            <input value={lastName} onChange={(event) => setLastName(event.target.value)} required />
          </label>
          </div>

          <div className="two-col register-form-row">
            <label className="register-account-field">
            Email
            <input value={email} onChange={(event) => setEmail(event.target.value)} required />
          </label>

            <label className="register-account-field">
              Role
              <select value={role} onChange={(event) => setRole(event.target.value as Role | "")} required>
                <option value="">Choose role</option>
                {(["STUDENT", "MENTOR", "ADMIN"] as Role[]).map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="two-col register-form-row">
            <label className="register-account-field">
              Password
              <input
                data-testid="admin-user-password-input"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="new-password"
              />
            </label>
            <label className="register-account-field">
              Confirm password
              <input
                data-testid="admin-user-confirm-password-input"
                type="password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                autoComplete="new-password"
              />
            </label>
          </div>

          <div className="two-col register-form-row">
            <label className="register-account-field">
              Status
              <select value={status} onChange={(event) => setStatus(event.target.value as AuthUser["status"])}>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </label>
            <div />
          </div>
        </div>

        {error ? <div className="alert error" data-testid="admin-user-edit-error">{error}</div> : null}

        <div className="action-row">
          <button className="primary-button" type="submit" disabled={saving} data-testid="admin-user-edit-save">
            {saving ? "Saving..." : "Save changes"}
          </button>
          <Link className="ghost-button" to={`/users/${entry.publicId}`}>
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
