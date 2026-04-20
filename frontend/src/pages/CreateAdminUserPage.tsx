import { FormEvent, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import type { AuthUser, Role } from "../types";

interface CreateAdminUserPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function CreateAdminUserPage({ token, user }: CreateAdminUserPageProps) {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [role, setRole] = useState<Role | "">("");
  const [status, setStatus] = useState<AuthUser["status"]>("ACTIVE");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token) {
      return;
    }

    setSaving(true);
    setError("");

    if (!password || !confirmPassword) {
      setError("Fill in both password fields to create a user.");
      setSaving(false);
      return;
    }
    if (password !== confirmPassword) {
      setError("Password and confirm password must match.");
      setSaving(false);
      return;
    }
    if (!role) {
      setError("Choose a role.");
      setSaving(false);
      return;
    }

    try {
      const createdUser = await api.createUser(token, {
        email,
        firstName,
        lastName,
        password,
        roles: [role],
        status
      });
      navigate(`/users/${createdUser.publicId}`, { replace: true, state: { flash: "User created." } });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create user");
      setSaving(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: "/users/new" }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell" data-testid="admin-user-create-page">
      <form className="panel stack gap-md register-form" onSubmit={onSubmit}>
        <div className="section-heading">
          <h1>Create account</h1>
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
                data-testid="admin-user-create-password-input"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="new-password"
              />
            </label>
            <label className="register-account-field">
              Confirm password
              <input
                data-testid="admin-user-create-confirm-password-input"
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

        {error ? <div className="alert error" data-testid="admin-user-create-error">{error}</div> : null}

        <div className="action-row">
          <button className="primary-button" type="submit" disabled={saving} data-testid="admin-user-create-save">
            {saving ? "Saving..." : "Create Account"}
          </button>
          <Link className="ghost-button" to="/users">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
