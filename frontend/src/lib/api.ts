import type {
  AuthResponse,
  AuthUser,
  Booking,
  Offer,
  PageResponse,
  Review,
  Role,
  Slot,
  Wallet,
  WalletTransaction
} from "../types";

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

const jsonHeaders = {
  "Content-Type": "application/json"
};

async function request<T>(path: string, init?: RequestInit, token?: string): Promise<T> {
  const headers = new Headers(init?.headers ?? {});
  if (!headers.has("Content-Type") && init?.body) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(path, {
    ...init,
    headers
  });

  if (!response.ok) {
    const text = await response.text();
    let message = `Request failed with status ${response.status}`;
    try {
      const parsed = JSON.parse(text) as { message?: string };
      if (parsed.message) {
        message = parsed.message;
      }
    } catch {
      if (text) {
        message = text;
      }
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const api = {
  register(payload: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    roles: Role[];
  }) {
    return request<{ userId: number; publicId: string; email: string; roles: Role[]; status: string }>(
      "/api/auth/register",
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      }
    );
  },

  login(payload: { email: string; password: string }) {
    return request<AuthResponse>("/api/auth/login", {
      method: "POST",
      headers: jsonHeaders,
      body: JSON.stringify(payload)
    });
  },

  getUser(token: string, userPublicId: string) {
    return request<AuthUser>(`/api/users/${userPublicId}`, undefined, token);
  },

  updateUser(
    token: string,
    userPublicId: string,
    payload: {
      email?: string;
      firstName?: string;
      lastName?: string;
      password?: string;
      displayName?: string;
      roles?: Role[];
      status?: AuthUser["status"];
    }
  ) {
    return request<AuthUser>(
      `/api/users/${userPublicId}`,
      {
        method: "PATCH",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  createUser(
    token: string,
    payload: {
      email: string;
      password: string;
      firstName: string;
      lastName: string;
      roles: Role[];
      status: AuthUser["status"];
    }
  ) {
    return request<AuthUser>(
      "/api/users",
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  getWallet(token: string, walletPublicId: string) {
    return request<Wallet>(`/api/wallets/${walletPublicId}`, undefined, token);
  },

  topUpWallet(token: string, walletPublicId: string, amount: number) {
    return request<Wallet>(
      `/api/wallets/${walletPublicId}/top-up`,
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify({ amount })
      },
      token
    );
  },

  getWalletTransactions(token: string, walletPublicId: string) {
    return request<WalletTransaction[]>(`/api/wallets/${walletPublicId}/transactions`, undefined, token);
  },

  getOffers(
    search?: string,
    sort?: "newest" | "priceAsc" | "priceDesc" | "titleAsc" | "titleDesc",
    category?: string
  ) {
    const params = new URLSearchParams();
    if (search) {
      params.set("search", search);
    }
    if (sort) {
      params.set("sort", sort);
    }
    if (category) {
      params.set("category", category);
    }
    const query = params.toString() ? `?${params.toString()}` : "";
    return request<PageResponse<Offer>>(`/api/offers${query}`);
  },

  getOffer(id: string | number) {
    return request<Offer>(`/api/offers/${id}`);
  },

  createOffer(
    token: string,
    payload: {
      title: string;
      description: string;
      category: string;
      durationMinutes: number;
      priceCredits: number;
      status: Offer["status"];
    }
  ) {
    return request<Offer>(
      "/api/offers",
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  updateOffer(
    token: string,
    offerPublicId: string,
    payload: {
      title?: string;
      description?: string;
      category?: string;
      durationMinutes?: number;
      priceCredits?: number;
    }
  ) {
    return request<Offer>(
      `/api/offers/${offerPublicId}`,
      {
        method: "PATCH",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  updateOfferStatus(token: string, offerPublicId: string, status: Offer["status"]) {
    return request<Offer>(
      `/api/offers/${offerPublicId}/status`,
      {
        method: "PATCH",
        headers: jsonHeaders,
        body: JSON.stringify({ status })
      },
      token
    );
  },

  getSlots(offerPublicId: string) {
    return request<Slot[]>(`/api/offers/${offerPublicId}/slots`);
  },

  createSlot(
    token: string,
    offerPublicId: string,
    payload: {
      date: string;
      startTime: string;
      endTime: string;
    }
  ) {
    return request<Slot[]>(
      `/api/offers/${offerPublicId}/slots`,
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  deleteSlot(token: string, slotId: number) {
    return request<void>(
      `/api/slots/${slotId}`,
      {
        method: "DELETE"
      },
      token
    );
  },

  createBooking(token: string, payload: { slotId: number; studentId: number }) {
    return request<Booking>(
      "/api/bookings",
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  listBookings(token: string) {
    return request<Booking[]>("/api/bookings", undefined, token);
  },

  getBooking(token: string, bookingPublicId: string) {
    return request<Booking>(`/api/bookings/${bookingPublicId}`, undefined, token);
  },

  cancelBooking(token: string, bookingPublicId: string) {
    return request<Booking>(
      `/api/bookings/${bookingPublicId}/cancel`,
      {
        method: "POST"
      },
      token
    );
  },

  completeBooking(token: string, bookingPublicId: string) {
    return request<Booking>(
      `/api/bookings/${bookingPublicId}/complete`,
      {
        method: "POST"
      },
      token
    );
  },

  getOfferReviews(offerPublicId: string) {
    return request<Review[]>(`/api/offers/${offerPublicId}/reviews`);
  },

  listReviews(token: string) {
    return request<Review[]>("/api/reviews", undefined, token);
  },

  getReview(token: string, reviewPublicId: string) {
    return request<Review>(`/api/reviews/${reviewPublicId}`, undefined, token);
  },

  createReview(token: string, bookingPublicId: string, payload: { rating: number; comment: string }) {
    return request<Review>(
      `/api/bookings/${bookingPublicId}/reviews`,
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  updateReview(token: string, reviewPublicId: string, payload: { rating: number; comment: string }) {
    return request<Review>(
      `/api/reviews/${reviewPublicId}`,
      {
        method: "PATCH",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  },

  deleteReview(token: string, reviewPublicId: string) {
    return request<void>(
      `/api/reviews/${reviewPublicId}`,
      {
        method: "DELETE"
      },
      token
    );
  },

  listUsers(token: string) {
    return request<AuthUser[]>("/api/users", undefined, token);
  },

  listMentors(token: string) {
    return request<AuthUser[]>("/api/users/mentors", undefined, token);
  },

  listStudents(token: string) {
    return request<AuthUser[]>("/api/users/students", undefined, token);
  },

  listManagedOffers(token: string) {
    return request<PageResponse<Offer>>("/api/offers?scope=all", undefined, token);
  },

  listOwnOffers(token: string) {
    return request<PageResponse<Offer>>("/api/offers?scope=mine", undefined, token);
  },

  createDirectReview(
    token: string,
    payload: {
      offerId: number;
      bookingId?: number | null;
      targetUserId?: number | null;
      rating: number;
      comment: string;
    }
  ) {
    return request<Review>(
      "/api/reviews",
      {
        method: "POST",
        headers: jsonHeaders,
        body: JSON.stringify(payload)
      },
      token
    );
  }
};
