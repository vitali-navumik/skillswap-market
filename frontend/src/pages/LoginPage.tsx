import { FormEvent, useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import type { AuthResponse } from "../types";

interface LoginPageProps {
  onAuthenticated: (response: AuthResponse) => void;
}

const demoAccounts = [
  {
    label: "Admin",
    email: "admin@test.com",
    password: "StrongPass1",
    roles: "ADMIN",
    description: "Best for reviewing users, bookings, reviews, and managing platform data."
  },
  {
    label: "Mentor",
    email: "mentor1@test.com",
    password: "StrongPass1",
    roles: "MENTOR",
    description: "Best for creating offers, opening slots, and completing bookings."
  },
  {
    label: "Student",
    email: "student1@test.com",
    password: "StrongPass1",
    roles: "STUDENT",
    description: "Best for booking offers, adding credits, and leaving reviews."
  }
] as const;

export function LoginPage({ onAuthenticated }: LoginPageProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = (location.state as {
    prefillEmail?: string;
    flash?: string;
  } | null) ?? null;
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(locationState?.flash ?? "");
  const [loading, setLoading] = useState(false);
  const [selectedDemoEmail, setSelectedDemoEmail] = useState("");

  useEffect(() => {
    if (locationState?.prefillEmail) {
      setEmail(locationState.prefillEmail);
    }
    if (locationState?.flash) {
      setSuccess(locationState.flash);
    }
  }, [locationState?.flash, locationState?.prefillEmail]);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const response = await api.login({ email, password });
      onAuthenticated(response);
      navigate("/offers", { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to log in");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-layout detail-shell">
      <div className="auth-panel">
        <span className="eyebrow">Welcome back</span>
        <h1>Log In</h1>
        <p>Use a seeded or newly registered account to continue.</p>

        <form className="stack gap-md auth-form" onSubmit={onSubmit}>
          <label>
            Email
            <input
              data-testid="login-email-input"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>

          <label>
            Password
            <input
              data-testid="login-password-input"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>

          {success ? <div className="alert success">{success}</div> : null}
          {error ? <div className="alert error">{error}</div> : null}

          <button className="primary-button" disabled={loading} type="submit">
            {loading ? "Logging in..." : "Log In"}
          </button>
        </form>

        <div className="stack gap-sm">
          <span className="label">Quick demo accounts</span>
          <div className="demo-account-grid">
            {demoAccounts.map((account) => (
              <button
                key={account.email}
                type="button"
                className="demo-account-card"
                onClick={() => {
                  setEmail(account.email);
                  setPassword(account.password);
                  setSelectedDemoEmail(account.email);
                  setError("");
                  setSuccess("");
                }}
              >
                <strong>{account.label}</strong>
                <span>{account.email}</span>
                <small>Password: {account.password}</small>
                <em>{account.roles}</em>
                <small>{account.description}</small>
              </button>
            ))}
          </div>
        </div>

        <p className="muted">
          Need an account? <Link to="/register">Create one here</Link>.
        </p>
      </div>
    </section>
  );
}
