import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { api, ApiError } from "../lib/api";
import { getFullName } from "../lib/userNames";
import type { AuthUser, Offer, Slot } from "../types";

interface CreateAdminBookingPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function CreateAdminBookingPage({ token, user }: CreateAdminBookingPageProps) {
  const navigate = useNavigate();
  const [students, setStudents] = useState<AuthUser[]>([]);
  const [offers, setOffers] = useState<Offer[]>([]);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [studentId, setStudentId] = useState<number | "">("");
  const [offerId, setOfferId] = useState<number | "">("");
  const [slotId, setSlotId] = useState<number | "">("");
  const [loading, setLoading] = useState(true);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      if (!token) {
        return;
      }

      setLoading(true);
      setError("");

      try {
        const [usersResult, offersResult] = await Promise.all([
          api.listUsers(token),
          api.listManagedOffers(token)
        ]);
        const eligibleStudents = usersResult.filter((entry) => entry.roles.includes("STUDENT") && entry.status === "ACTIVE");
        const activeOffers = offersResult.content.filter((entry) => entry.status === "ACTIVE");
        setStudents(eligibleStudents);
        setOffers(activeOffers);
        setStudentId(eligibleStudents[0]?.id ?? "");
        setOfferId(activeOffers[0]?.id ?? "");
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load booking create form");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [token]);

  useEffect(() => {
    const loadSlots = async () => {
      if (offerId === "") {
        setSlots([]);
        setSlotId("");
        return;
      }

      setSlotsLoading(true);
      setError("");

      try {
        const offerPublicId = offers.find((entry) => entry.id === offerId)?.publicId;
        if (!offerPublicId) {
          setSlots([]);
          setSlotId("");
          return;
        }
        const slotResult = await api.getSlots(offerPublicId);
        const openSlots = slotResult.filter((entry) => entry.status === "OPEN");
        setSlots(openSlots);
        setSlotId((current) => (current !== "" && openSlots.some((entry) => entry.id === current) ? current : (openSlots[0]?.id ?? "")));
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load slots");
        setSlots([]);
        setSlotId("");
      } finally {
        setSlotsLoading(false);
      }
    };

    void loadSlots();
  }, [offerId]);

  const selectedStudent = useMemo(
    () => students.find((entry) => entry.id === studentId) ?? null,
    [studentId, students]
  );
  const selectedOffer = useMemo(
    () => offers.find((entry) => entry.id === offerId) ?? null,
    [offerId, offers]
  );
  const selectedSlot = useMemo(
    () => slots.find((entry) => entry.id === slotId) ?? null,
    [slotId, slots]
  );

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token || studentId === "" || slotId === "") {
      return;
    }

    setSaving(true);
    setError("");

    try {
      const created = await api.createBooking(token, { slotId, studentId });
      navigate(`/bookings/${created.publicId}`, {
        replace: true,
        state: {
          flash: `Booking created for ${
            selectedStudent
              ? getFullName(selectedStudent.firstName, selectedStudent.lastName, selectedStudent.displayName)
              : `User #${studentId}`
          }.`
        }
      });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create booking");
      setSaving(false);
    }
  };

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: "/bookings/new" }} />;
  }

  if (!user.roles.includes("ADMIN")) {
    return <div className="panel">Admin access required.</div>;
  }

  if (loading) {
    return <div className="panel" data-testid="admin-booking-create-loading">Loading booking create page...</div>;
  }

  if (students.length === 0) {
    return (
      <div className="panel" data-testid="admin-booking-create-empty-students">
        No active users with the `STUDENT` role are available for booking creation.
      </div>
    );
  }

  if (offers.length === 0) {
    return (
      <div className="panel" data-testid="admin-booking-create-empty-offers">
        No active offers are available for booking creation.
      </div>
    );
  }

  return (
    <div className="stack gap-xl" data-testid="admin-booking-create-page">
      <BackButton fallbackTo="/bookings" testId="admin-booking-create-back-link" />
      <section className="panel section-stack">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Admin booking workspace</span>
            <h1>Create booking</h1>
          </div>
        </div>
        <p className="muted">
          Create a booking for a selected student. Wallet reserve and later settlement will apply to that student.
        </p>
      </section>

      <form className="panel stack gap-md" onSubmit={onSubmit}>
        <div className="section-heading">
          <h2>Booking fields</h2>
          <span>Save only after the student and slot look correct.</span>
        </div>

        <label>
          Student
          <select
            data-testid="admin-booking-create-student"
            value={studentId}
            onChange={(event) => setStudentId(event.target.value ? Number(event.target.value) : "")}
          >
            {students.map((entry) => (
              <option key={entry.id} value={entry.id}>
                {getFullName(entry.firstName, entry.lastName, entry.displayName)}
              </option>
            ))}
          </select>
        </label>

        <label>
          Offer
          <select
            data-testid="admin-booking-create-offer"
            value={offerId}
            onChange={(event) => setOfferId(event.target.value ? Number(event.target.value) : "")}
          >
            {offers.map((entry) => (
              <option key={entry.id} value={entry.id}>
                {entry.title} | {entry.mentorDisplayName}
              </option>
            ))}
          </select>
        </label>

        <label>
          Open slot
          <select
            data-testid="admin-booking-create-slot"
            value={slotId}
            onChange={(event) => setSlotId(event.target.value ? Number(event.target.value) : "")}
            disabled={slotsLoading || slots.length === 0}
          >
            {slots.length === 0 ? (
              <option value="">{slotsLoading ? "Loading open slots..." : "No open slots"}</option>
            ) : (
              slots.map((entry) => (
                <option key={entry.id} value={entry.id}>
                  OPEN | {new Date(entry.startTime).toLocaleString()} - {new Date(entry.endTime).toLocaleString()}
                </option>
              ))
            )}
          </select>
        </label>

        <small className="inline-note" data-testid="admin-booking-create-slot-note">
          Only free `OPEN` slots are shown in this list.
        </small>

        {(selectedStudent || selectedOffer || selectedSlot) ? (
          <div className="offer-preview-card" data-testid="admin-booking-create-preview">
            <div className="section-heading">
              <h2>Preview</h2>
              <span>Booking will be created for the selected student</span>
            </div>
            <div className="offer-meta">
              {selectedStudent ? (
                <span>Student: {getFullName(selectedStudent.firstName, selectedStudent.lastName, selectedStudent.displayName)}</span>
              ) : null}
              {selectedOffer ? <span>Offer: {selectedOffer.title}</span> : null}
              {selectedOffer ? <span>{selectedOffer.priceCredits} credits</span> : null}
            </div>
            {selectedSlot ? (
              <p className="muted">
                {new Date(selectedSlot.startTime).toLocaleString()} - {new Date(selectedSlot.endTime).toLocaleString()}
              </p>
            ) : (
              <p className="muted">Select an open slot to continue.</p>
            )}
          </div>
        ) : null}

        {error ? <div className="alert error" data-testid="admin-booking-create-error">{error}</div> : null}

        <div className="action-row">
          <button
            className="primary-button"
            type="submit"
            disabled={saving || studentId === "" || slotId === ""}
            data-testid="admin-booking-create-submit"
          >
            {saving ? "Creating..." : "Create booking"}
          </button>
          <Link className="ghost-button" to="/bookings">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}
