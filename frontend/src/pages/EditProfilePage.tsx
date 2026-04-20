import { FormEvent, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import type { AuthUser } from "../types";

interface EditProfilePageProps {
  token: string | null;
  user: AuthUser | null;
  onUserUpdated: (user: AuthUser) => void;
}

export function EditProfilePage({ token, user, onUserUpdated }: EditProfilePageProps) {
  const navigate = useNavigate();
  const params = useParams<{ userPublicId: string }>();
  const userPublicId = params.userPublicId ?? "";
  const [email, setEmail] = useState(user?.email ?? "");
  const [firstName, setFirstName] = useState(user?.firstName ?? "");
  const [lastName, setLastName] = useState(user?.lastName ?? "");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [feedback, setFeedback] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [saving, setSaving] = useState(false);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token) {
      return;
    }

    setSaving(true);
    setFeedback(null);
    if (password || confirmPassword) {
      if (!password || !confirmPassword) {
        setFeedback({
          tone: "error",
          text: "Fill in both password fields to change your password."
        });
        setSaving(false);
        return;
      }
      if (password !== confirmPassword) {
        setFeedback({
          tone: "error",
          text: "Password and confirm password must match."
        });
        setSaving(false);
        return;
      }
    }
    try {
      const updatedUser = await api.updateUser(token, user!.publicId, {
        email,
        firstName,
        lastName,
        password: password || undefined
      });
      onUserUpdated(updatedUser);
      navigate(`/users/${user!.publicId}`, { replace: true, state: { flash: "Profile updated." } });
    } catch (err) {
      setFeedback({
        tone: "error",
        text: err instanceof ApiError ? err.message : "Failed to update profile"
      });
      setSaving(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: `/users/${userPublicId}/edit` }} />;
  }

  if (!userPublicId || userPublicId !== user.publicId) {
    return <div className="panel">This profile editor is available only for the signed-in user.</div>;
  }

  return (
    <div className="stack gap-xl detail-shell" data-testid="profile-edit-page">
      <BackButton fallbackTo={`/users/${user.publicId}`} testId="profile-edit-back-link" />
      <form className="panel stack gap-md user-edit-form" onSubmit={onSubmit} data-testid="profile-edit-form">
        <div className="section-heading user-edit-heading">
          <h1>Edit profile</h1>
        </div>

        <div className="two-col">
          <label className="user-edit-field">
            First name
            <input
              data-testid="profile-first-name-input"
              value={firstName}
              onChange={(event) => setFirstName(event.target.value)}
              required
            />
          </label>
          <label className="user-edit-field">
            Last name
            <input
              data-testid="profile-last-name-input"
              value={lastName}
              onChange={(event) => setLastName(event.target.value)}
              required
            />
          </label>
        </div>

        <label className="user-edit-field user-edit-field-email">
          Email
          <input
            data-testid="profile-email-input"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>

        <label>
          Roles
          <div className="pill-list">
            {user.roles.map((role) => (
              <label className="checkbox-row" key={role}>
                <input type="checkbox" checked disabled />
                {role}
              </label>
            ))}
          </div>
        </label>

        <div className="inline-note">
          Roles are shown for context. Password changes are applied only when you fill both password fields.
        </div>

        <div className="stack gap-sm user-password-section">
          <div className="section-heading user-edit-subheading">
            <h2>Password</h2>
          </div>

          <div className="two-col">
            <label className="user-edit-field">
              New password
              <input
                data-testid="profile-password-input"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="new-password"
              />
            </label>
            <label className="user-edit-field">
              Confirm password
              <input
                data-testid="profile-confirm-password-input"
                type="password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                autoComplete="new-password"
              />
            </label>
          </div>
        </div>

        {feedback ? <div className={`alert ${feedback.tone}`} data-testid="profile-feedback">{feedback.text}</div> : null}

        <div className="action-row">
          <button className="primary-button" type="submit" disabled={saving} data-testid="profile-save-button">
            {saving ? "Saving..." : "Save changes"}
          </button>
          <Link className="ghost-button" to={`/users/${user.publicId}`}>
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
