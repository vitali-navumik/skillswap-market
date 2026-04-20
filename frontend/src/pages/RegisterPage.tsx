import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import type { Role } from "../types";

const selectableRoles: { value: Role; label: string }[] = [
  { value: "STUDENT", label: "Student" },
  { value: "MENTOR", label: "Mentor" }
];

export function RegisterPage() {
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [role, setRole] = useState<Role | "">("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    if (password !== confirmPassword) {
      setError("Password and confirm password must match");
      setLoading(false);
      return;
    }

    if (!role) {
      setError("Choose a role");
      setLoading(false);
      return;
    }

    try {
      await api.register({ firstName, lastName, email, password, roles: [role] });
      setSuccess("Registration complete. You can log in now.");
      setTimeout(
        () =>
          navigate("/login", {
            state: {
              prefillEmail: email,
              flash: "Registration complete. Log in to continue from the catalog."
            }
          }),
        700
      );
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="register-layout detail-shell">
      <div className="register-panel wide">
        <span className="eyebrow">Get started</span>
        <h1>Create Account</h1>

        <form className="stack gap-md register-form" onSubmit={onSubmit}>
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
                <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
              </label>

              <label className="register-account-field">
                Role
                <select value={role} onChange={(event) => setRole(event.target.value as Role | "")} required>
                  <option value="">Choose role</option>
                  {selectableRoles.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <div className="two-col register-form-row">
              <label className="register-account-field">
                Password
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="At least 8 chars, upper, lower, digit"
                  required
                />
              </label>

              <label className="register-account-field">
                Confirm password
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  required
                />
              </label>
            </div>
          </div>

          {error ? <div className="alert error">{error}</div> : null}
          {success ? <div className="alert success">{success}</div> : null}

          <button className="primary-button" type="submit" disabled={loading}>
            {loading ? "Creating account..." : "Create Account"}
          </button>
        </form>

        <p className="muted">
          Prefer a ready-made walkthrough? <a href="/login">Use the demo accounts instead</a>.
        </p>
      </div>
    </section>
  );
}
