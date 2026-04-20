import { useEffect, useRef, useState } from "react";
import { Link, NavLink } from "react-router-dom";
import type { AuthUser } from "../types";
import { getFullName } from "../lib/userNames";

interface ShellProps {
  user: AuthUser | null;
  onLogout: () => void;
  children: React.ReactNode;
}

export function Shell({ user, onLogout, children }: ShellProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);
  const canMentor = user?.roles.includes("MENTOR");
  const isAdmin = user?.roles.includes("ADMIN");
  const canCreateOffer = Boolean(canMentor && !isAdmin);
  const showMyOffers = Boolean(canMentor && !isAdmin);
  const isAuthenticated = Boolean(user);
  const bookingsLabel = isAdmin ? "Bookings" : "My Bookings";
  const walletLabel = isAdmin ? "Wallets" : "Wallet";
  const walletPath = isAdmin ? "/wallets" : user?.walletPublicId ? `/wallets/${user.walletPublicId}` : "/login";
  const profilePath = user ? `/users/${user.publicId}` : "/login";
  const bookingsPath = isAuthenticated ? "/bookings" : "/login";
  const showPersonalWallet = Boolean(user && !isAdmin && user.walletPublicId);

  useEffect(() => {
    if (!menuOpen) {
      return;
    }

    const handlePointerDown = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setMenuOpen(false);
      }
    };

    window.addEventListener("mousedown", handlePointerDown);
    window.addEventListener("keydown", handleEscape);
    return () => {
      window.removeEventListener("mousedown", handlePointerDown);
      window.removeEventListener("keydown", handleEscape);
    };
  }, [menuOpen]);

  const handleLogout = () => {
    localStorage.removeItem("skillswap.token");
    localStorage.removeItem("skillswap.user");
    onLogout();
    window.location.replace("/offers");
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/offers" className="brand">
          <span className="brand-mark">S</span>
          <div>
            <strong>SkillSwap Market</strong>
            <small>Demo MVP preview</small>
          </div>
        </Link>

        <nav className="nav" data-testid="app-nav">
          <NavLink to="/offers" end data-testid="nav-catalog">Catalog</NavLink>
          {isAdmin ? <NavLink to="/users" data-testid="nav-users">Users</NavLink> : null}
          {showMyOffers ? <NavLink to="/my-offers" data-testid="nav-my-offers">My Offers</NavLink> : null}
          {isAdmin ? <NavLink to={walletPath} data-testid="nav-wallet">{walletLabel}</NavLink> : null}
          {isAuthenticated ? <NavLink to={bookingsPath} end data-testid="nav-bookings">{bookingsLabel}</NavLink> : null}
          {isAdmin ? <NavLink to="/reviews" end data-testid="nav-reviews">Reviews</NavLink> : null}
          {isAdmin ? <NavLink to="/users/new" data-testid="nav-create-account">Create Account</NavLink> : null}
          {canCreateOffer ? <NavLink to="/offers/new" className="nav-create-link" data-testid="nav-create-offer">Create Offer</NavLink> : null}
        </nav>

        <div className="topbar-actions">
          {user ? (
            <div className="user-menu" ref={menuRef}>
              <button
                type="button"
                className="user-pill user-pill-button"
                data-testid="topbar-user-pill"
                onClick={() => setMenuOpen((current) => !current)}
                aria-haspopup="menu"
                aria-expanded={menuOpen}
              >
                <span className="user-pill-main">
                  <span>{getFullName(user.firstName, user.lastName, user.displayName)}</span>
                  <span className={`user-pill-chevron ${menuOpen ? "open" : ""}`} aria-hidden="true">
                    ▾
                  </span>
                </span>
                <small>{user.roles.join(" + ")}</small>
              </button>

              {menuOpen ? (
                <div className="user-menu-dropdown" data-testid="topbar-user-dropdown" role="menu">
                  <Link className="user-menu-item" to={profilePath} role="menuitem" onClick={() => setMenuOpen(false)}>
                    My Profile
                  </Link>
                  {showPersonalWallet ? (
                    <Link className="user-menu-item" to={walletPath} role="menuitem" onClick={() => setMenuOpen(false)}>
                      My Wallet
                    </Link>
                  ) : null}
                  <button className="user-menu-item user-menu-item-button" onClick={handleLogout} data-testid="logout-button" role="menuitem">
                    Log Out
                  </button>
                </div>
              ) : null}
            </div>
          ) : (
            <>
              <Link className="ghost-button" to="/login" data-testid="nav-login">
                Log In
              </Link>
              <Link className="ghost-button topbar-join-button" to="/register" data-testid="nav-register">
                Create Account
              </Link>
            </>
          )}
        </div>
      </header>

      <main className="page">{children}</main>
    </div>
  );
}
