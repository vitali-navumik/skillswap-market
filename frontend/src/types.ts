export type Role = "STUDENT" | "MENTOR" | "ADMIN";
export type UserStatus = "ACTIVE" | "INACTIVE";
export type OfferStatus = "DRAFT" | "ACTIVE" | "ARCHIVED" | "BLOCKED";
export type SlotStatus = "OPEN" | "BOOKED";
export type BookingStatus = "RESERVED" | "COMPLETED" | "CANCELLED";
export type TransactionType =
  | "TOP_UP"
  | "CHARGE"
  | "REFUND"
  | "PAYOUT"
  | "ADJUSTMENT";
export type TransactionStatus = "CREATED" | "COMPLETED" | "FAILED";

export interface AuthUser {
  id: number;
  publicId: string;
  walletPublicId: string | null;
  email: string;
  firstName: string;
  lastName: string;
  displayName: string;
  roles: Role[];
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
}

export interface Offer {
  id: number;
  publicId: string;
  mentorId: number;
  mentorPublicId: string;
  mentorDisplayName: string;
  title: string;
  description: string;
  category: string;
  durationMinutes: number;
  priceCredits: number;
  cancellationPolicyHours: number;
  status: OfferStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Slot {
  id: number;
  offerId: number;
  offerPublicId: string;
  startTime: string;
  endTime: string;
  status: SlotStatus;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Wallet {
  id: number;
  publicId: string;
  userId: number;
  userPublicId: string;
  balance: number;
  reservedBalance: number;
  availableBalance: number;
  updatedAt: string;
}

export interface WalletTransaction {
  id: number;
  walletId: number;
  bookingId: number | null;
  bookingPublicId: string | null;
  type: TransactionType;
  amount: number;
  status: TransactionStatus;
  createdAt: string;
}

export interface Booking {
  id: number;
  publicId: string;
  slotId: number;
  offerId: number;
  offerPublicId: string;
  offerTitle: string;
  studentId: number;
  studentPublicId: string;
  studentDisplayName: string;
  mentorId: number;
  mentorPublicId: string;
  mentorDisplayName: string;
  status: BookingStatus;
  priceCredits: number;
  reservedAmount: number;
  cancelledByUserId: number | null;
  cancelledByUserPublicId: string | null;
  slotStartTime: string;
  slotEndTime: string;
  createdAt: string;
  updatedAt: string;
}

export interface Review {
  id: number;
  publicId: string;
  offerId: number;
  offerPublicId: string;
  offerTitle: string;
  bookingId: number | null;
  bookingPublicId: string | null;
  authorId: number;
  authorPublicId: string;
  authorDisplayName: string;
  targetUserId: number;
  targetUserPublicId: string;
  targetUserDisplayName: string;
  rating: number;
  comment: string;
  createdInAdminScope: boolean;
  createdAt: string;
  updatedAt: string;
}
