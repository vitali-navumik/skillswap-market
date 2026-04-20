import { useEffect, useMemo, useState } from "react";
import { Navigate, Route, Routes, useLocation, useParams } from "react-router-dom";
import { AdminBookingDetailsPage } from "./pages/AdminBookingDetailsPage";
import { AdminReviewsPage } from "./pages/AdminReviewsPage";
import { AdminUserDetailsPage } from "./pages/AdminUserDetailsPage";
import { AdminUsersPage } from "./pages/AdminUsersPage";
import { BookingDetailsPage } from "./pages/BookingDetailsPage";
import { Shell } from "./components/Shell";
import { BookingsPage } from "./pages/BookingsPage";
import { CatalogPage } from "./pages/CatalogPage";
import { CreateOfferPage } from "./pages/CreateOfferPage";
import { CreateAdminUserPage } from "./pages/CreateAdminUserPage";
import { CreateReviewPage } from "./pages/CreateReviewPage";
import { EditAdminReviewPage } from "./pages/EditAdminReviewPage";
import { EditAdminUserPage } from "./pages/EditAdminUserPage";
import { EditProfilePage } from "./pages/EditProfilePage";
import { EditOfferPage } from "./pages/EditOfferPage";
import { EditReviewPage } from "./pages/EditReviewPage";
import { LoginPage } from "./pages/LoginPage";
import { OfferDetailsPage } from "./pages/OfferDetailsPage";
import { ProfilePage } from "./pages/ProfilePage";
import { RegisterPage } from "./pages/RegisterPage";
import { ReviewDetailsPage } from "./pages/ReviewDetailsPage";
import { WalletPage } from "./pages/WalletPage";
import type { AuthResponse, AuthUser } from "./types";

function loadAuth(): { token: string | null; user: AuthUser | null } {
  const token = localStorage.getItem("skillswap.token");
  const userRaw = localStorage.getItem("skillswap.user");
  const user = userRaw ? (JSON.parse(userRaw) as AuthUser) : null;
  if (user && user.roles.length !== 1) {
    return {
      token: null,
      user: null
    };
  }
  return {
    token,
    user
  };
}

function ProtectedRoute({
  user,
  children
}: {
  user: AuthUser | null;
  children: React.ReactNode;
}) {
  const location = useLocation();
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <>{children}</>;
}

function UserDetailsRoute({
  token,
  user
}: {
  token: string | null;
  user: AuthUser | null;
}) {
  const params = useParams<{ userPublicId: string }>();
  const userPublicId = params.userPublicId ?? "";
  const isAdminViewingForeignUser = Boolean(user?.roles.includes("ADMIN") && userPublicId && userPublicId !== user.publicId);

  return isAdminViewingForeignUser ? (
    <AdminUserDetailsPage token={token} user={user} />
  ) : (
    <ProfilePage token={token} user={user} />
  );
}

function UserEditRoute({
  token,
  user,
  onUserUpdated
}: {
  token: string | null;
  user: AuthUser | null;
  onUserUpdated: (user: AuthUser) => void;
}) {
  const params = useParams<{ userPublicId: string }>();
  const userPublicId = params.userPublicId ?? "";
  const isAdminEditing = Boolean(user?.roles.includes("ADMIN"));

  return isAdminEditing ? (
    <EditAdminUserPage token={token} user={user} onUserUpdated={onUserUpdated} />
  ) : (
    <EditProfilePage token={token} user={user} onUserUpdated={onUserUpdated} />
  );
}

function ReviewDetailsRoute({
  token,
  user
}: {
  token: string | null;
  user: AuthUser | null;
}) {
  return <ReviewDetailsPage token={token} user={user} />;
}

function ReviewEditRoute({
  token,
  user
}: {
  token: string | null;
  user: AuthUser | null;
}) {
  return user?.roles.includes("ADMIN") ? (
    <EditAdminReviewPage token={token} user={user} />
  ) : (
    <EditReviewPage token={token} user={user} />
  );
}

function OffersListRoute({
  token,
  user
}: {
  token: string | null;
  user: AuthUser | null;
}) {
  return <CatalogPage token={token} user={user} mode="catalog" />;
}

function MyOffersRoute({
  token,
  user
}: {
  token: string | null;
  user: AuthUser | null;
}) {
  if (!user?.roles.includes("MENTOR")) {
    return <Navigate to="/offers" replace />;
  }

  return <CatalogPage token={token} user={user} mode="myOffers" />;
}

export default function App() {
  const [register, setAuth] = useState(loadAuth);

  useEffect(() => {
    if (register.token && register.user) {
      localStorage.setItem("skillswap.token", register.token);
      localStorage.setItem("skillswap.user", JSON.stringify(register.user));
    } else {
      localStorage.removeItem("skillswap.token");
      localStorage.removeItem("skillswap.user");
    }
  }, [register]);

  const onAuthenticated = (response: AuthResponse) => {
    setAuth({
      token: response.accessToken,
      user: response.user
    });
  };

  const onUserUpdated = (user: AuthUser) => {
    setAuth((current) => ({
      token: current.token,
      user
    }));
  };

  const onLogout = () => setAuth({ token: null, user: null });

  const titleTone = useMemo(() => {
    if (register.user?.roles.includes("ADMIN")) {
      return "admin";
    }
    if (register.user?.roles.includes("MENTOR")) {
      return "mentor";
    }
    return "default";
  }, [register.user]);

  return (
    <div className={`theme-${titleTone}`}>
      <Shell user={register.user} onLogout={onLogout}>
        <Routes>
          <Route path="/offers" element={<OffersListRoute token={register.token} user={register.user} />} />
          <Route
            path="/my-offers"
            element={
              <ProtectedRoute user={register.user}>
                <MyOffersRoute token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<LoginPage onAuthenticated={onAuthenticated} />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/offers/:offerPublicId" element={<OfferDetailsPage token={register.token} user={register.user} />} />
          <Route
            path="/wallets"
            element={
              <ProtectedRoute user={register.user}>
                <WalletPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/wallets/:walletPublicId"
            element={
              <ProtectedRoute user={register.user}>
                <WalletPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/bookings"
            element={
              <ProtectedRoute user={register.user}>
                <BookingsPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/bookings/:bookingPublicId"
            element={
              <ProtectedRoute user={register.user}>
                {register.user?.roles.includes("ADMIN") ? (
                  <AdminBookingDetailsPage token={register.token} user={register.user} />
                ) : (
                  <BookingDetailsPage token={register.token} user={register.user} />
                )}
              </ProtectedRoute>
            }
          />
          <Route
            path="/bookings/:bookingPublicId/review/new"
            element={
              <ProtectedRoute user={register.user}>
                <CreateReviewPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reviews"
            element={
              <ProtectedRoute user={register.user}>
                <AdminReviewsPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reviews/:reviewPublicId"
            element={
              <ProtectedRoute user={register.user}>
                <ReviewDetailsRoute token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reviews/:reviewPublicId/edit"
            element={
              <ProtectedRoute user={register.user}>
                <ReviewEditRoute token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/offers/new"
            element={
              <ProtectedRoute user={register.user}>
                {register.user?.roles.includes("ADMIN") ? (
                  <Navigate to="/offers" replace />
                ) : (
                  <CreateOfferPage token={register.token} user={register.user} />
                )}
              </ProtectedRoute>
            }
          />
          <Route
            path="/offers/:offerPublicId/edit"
            element={
              <ProtectedRoute user={register.user}>
                <EditOfferPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route path="/dashboard" element={<Navigate to="/users" replace />} />
          <Route
            path="/users"
            element={
              <ProtectedRoute user={register.user}>
                <AdminUsersPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/users/new"
            element={
              <ProtectedRoute user={register.user}>
                <CreateAdminUserPage token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/users/:userPublicId"
            element={
              <ProtectedRoute user={register.user}>
                <UserDetailsRoute token={register.token} user={register.user} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/users/:userPublicId/edit"
            element={
              <ProtectedRoute user={register.user}>
                <UserEditRoute token={register.token} user={register.user} onUserUpdated={onUserUpdated} />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<div className="panel">Page not found.</div>} />
        </Routes>
      </Shell>
    </div>
  );
}
