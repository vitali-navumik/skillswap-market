import { useEffect, useMemo, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import { getFullName } from "../lib/userNames";
import type { AuthUser, Booking } from "../types";

type BookingSortOrder = "dateAsc" | "dateDesc";

interface BookingsPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function BookingsPage({ token, user }: BookingsPageProps) {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [adminUsers, setAdminUsers] = useState<AuthUser[]>([]);
  const [mentorDirectory, setMentorDirectory] = useState<AuthUser[]>([]);
  const [studentDirectory, setStudentDirectory] = useState<AuthUser[]>([]);
  const [studentFilterId, setStudentFilterId] = useState<number | "">("");
  const [mentorFilterId, setMentorFilterId] = useState<number | "">("");
  const [statusFilter, setStatusFilter] = useState<Booking["status"] | "">("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [sortOrder, setSortOrder] = useState<BookingSortOrder>("dateDesc");
  const [feedback, setFeedback] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [loading, setLoading] = useState(true);
  const location = useLocation();
  const locationState = (location.state as { flash?: string } | null) ?? null;
  const isAdmin = user?.roles.includes("ADMIN") ?? false;
  const hasStudentRole = user?.roles.includes("STUDENT") ?? false;
  const hasMentorRole = user?.roles.includes("MENTOR") ?? false;
  const isStudentView = !isAdmin && hasStudentRole;
  const isMentorView = !isAdmin && hasMentorRole;
  const showStudentFilter = isAdmin || isMentorView;
  const showMentorFilter = isAdmin || isStudentView;
  const statusOptions: Booking["status"][] = ["RESERVED", "COMPLETED", "CANCELLED"];

  const load = async () => {
    if (!token) {
      return;
    }

    setLoading(true);
      try {
        if (isAdmin) {
          const [usersResult, bookingResult] = await Promise.all([
            api.listUsers(token),
            api.listBookings(token)
          ]);
          setAdminUsers(usersResult);
          setBookings(bookingResult);
        } else {
          const [bookingResult, mentorResult, studentResult] = await Promise.all([
            api.listBookings(token),
            showMentorFilter ? api.listMentors(token) : Promise.resolve([]),
            showStudentFilter ? api.listStudents(token) : Promise.resolve([])
          ]);
          setBookings(bookingResult);
          setMentorDirectory(mentorResult);
          setStudentDirectory(studentResult);
        }
    } catch (err) {
      setFeedback({
        tone: "error",
        text: err instanceof ApiError ? err.message : "Failed to load bookings"
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [hasMentorRole, isAdmin, showMentorFilter, token, user]);

  const studentOptions = useMemo(() => {
    if (isAdmin) {
      return adminUsers
        .filter((entry) => entry.roles.includes("STUDENT"))
        .map((entry) => ({ id: entry.id, label: getFullName(entry.firstName, entry.lastName, entry.displayName) }));
    }

    if (isMentorView) {
      return studentDirectory.map((entry) => ({
        id: entry.id,
        label: getFullName(entry.firstName, entry.lastName, entry.displayName)
      }));
    }

    const seen = new Map<number, string>();
    bookings.forEach((booking) => {
      if (!seen.has(booking.studentId)) {
        seen.set(booking.studentId, booking.studentDisplayName);
      }
    });
    return Array.from(seen.entries()).map(([id, label]) => ({ id, label }));
  }, [adminUsers, bookings, isAdmin, isMentorView, studentDirectory]);

  const mentorOptions = useMemo(() => {
    if (isAdmin) {
      return adminUsers
        .filter((entry) => entry.roles.includes("MENTOR"))
        .map((entry) => ({ id: entry.id, label: getFullName(entry.firstName, entry.lastName, entry.displayName) }));
    }

    if (isStudentView) {
      return mentorDirectory.map((entry) => ({
        id: entry.id,
        label: getFullName(entry.firstName, entry.lastName, entry.displayName)
      }));
    }

    const seen = new Map<number, string>();
    bookings.forEach((booking) => {
      if (!seen.has(booking.mentorId)) {
        seen.set(booking.mentorId, booking.mentorDisplayName);
      }
    });
    return Array.from(seen.entries()).map(([id, label]) => ({ id, label }));
  }, [adminUsers, bookings, isAdmin, isStudentView, mentorDirectory]);

  const getStatusClass = (value: string) => value.toLowerCase().replace(/_/g, "-");
  const filteredBookings = bookings
    .filter((booking) => {
      const studentMatch = studentFilterId === "" || booking.studentId === studentFilterId;
      const mentorMatch = mentorFilterId === "" || booking.mentorId === mentorFilterId;
      const statusMatch = statusFilter === "" || booking.status === statusFilter;
      const bookingDate = booking.slotStartTime.slice(0, 10);
      const fromMatch = !dateFrom || bookingDate >= dateFrom;
      const toMatch = !dateTo || bookingDate <= dateTo;

      return studentMatch && mentorMatch && statusMatch && fromMatch && toMatch;
    })
    .sort((left, right) => {
      const leftTime = new Date(left.slotStartTime).getTime();
      const rightTime = new Date(right.slotStartTime).getTime();
      return sortOrder === "dateAsc" ? leftTime - rightTime : rightTime - leftTime;
    });
  const hasSidebarFilters = Boolean(studentFilterId !== "" || mentorFilterId !== "" || statusFilter || dateFrom || dateTo);

  const clearSidebarFilters = () => {
    setStudentFilterId("");
    setMentorFilterId("");
    setStatusFilter("");
    setDateFrom("");
    setDateTo("");
  };

  const isAllPresetActive = statusFilter === "";
  const isReservedPresetActive = statusFilter === "RESERVED";
  const isCompletedPresetActive = statusFilter === "COMPLETED";

  const applyQuickPreset = (preset: "all" | "reserved" | "completed") => {
    switch (preset) {
      case "reserved":
        setStatusFilter("RESERVED");
        break;
      case "completed":
        setStatusFilter("COMPLETED");
        break;
      case "all":
      default:
        setStatusFilter("");
        break;
    }
  };

  if (!token || !user) {
    return <div className="panel">Please log in to view your bookings.</div>;
  }

  if (loading) {
    return <div className="panel">Loading bookings...</div>;
  }

  return (
    <div className="stack gap-xl list-page-shell">
      <div className={showStudentFilter || showMentorFilter ? "filter-layout" : "stack"}>
        {showStudentFilter || showMentorFilter ? (
          <aside className="panel filter-sidebar" data-testid="bookings-filters-panel">
            <div className="stack gap-md">
              <div className="sidebar-heading">
                <strong>Filters</strong>
              </div>
              <div className="bookings-filter-grid">
                {showStudentFilter ? (
                  <label>
                    Student
                    <select
                      aria-label="Filter by student"
                      className="bookings-filter-select"
                      data-testid="bookings-student-filter"
                      value={studentFilterId}
                      onChange={(event) => setStudentFilterId(event.target.value ? Number(event.target.value) : "")}
                    >
                      <option value="">All students</option>
                      {studentOptions.map((entry) => (
                        <option key={entry.id} value={entry.id}>
                          {entry.label}
                        </option>
                      ))}
                    </select>
                  </label>
                ) : null}
                {showMentorFilter ? (
                  <label>
                    Mentor
                    <select
                      aria-label="Filter by mentor"
                      className="bookings-filter-select"
                      data-testid="bookings-mentor-filter"
                      value={mentorFilterId}
                      onChange={(event) => setMentorFilterId(event.target.value ? Number(event.target.value) : "")}
                    >
                      <option value="">All mentors</option>
                      {mentorOptions.map((entry) => (
                        <option key={entry.id} value={entry.id}>
                          {entry.label}
                        </option>
                      ))}
                    </select>
                  </label>
                ) : null}
                <label>
                  Status
                  <select
                    aria-label="Filter by booking status"
                    className="bookings-filter-select"
                    data-testid="bookings-status-filter"
                    value={statusFilter}
                    onChange={(event) => setStatusFilter(event.target.value as Booking["status"] | "")}
                  >
                    <option value="">All statuses</option>
                    {statusOptions.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Date from
                  <input
                    type="date"
                    data-testid="bookings-date-from-filter"
                    value={dateFrom}
                    onChange={(event) => setDateFrom(event.target.value)}
                  />
                </label>
                <label>
                  Date to
                  <input
                    type="date"
                    data-testid="bookings-date-to-filter"
                    value={dateTo}
                    onChange={(event) => setDateTo(event.target.value)}
                  />
                </label>
                <label>
                  Sort
                  <select
                    aria-label="Sort bookings by date"
                    className="bookings-filter-select"
                    data-testid="bookings-sort-filter"
                    value={sortOrder}
                    onChange={(event) => setSortOrder(event.target.value as BookingSortOrder)}
                  >
                    <option value="dateAsc">Date: ascending</option>
                    <option value="dateDesc">Date: descending</option>
                  </select>
                </label>
              </div>
              {hasSidebarFilters ? (
                <button type="button" className="text-link" data-testid="bookings-clear-filters" onClick={clearSidebarFilters}>
                  Clear all
                </button>
              ) : null}
            </div>
          </aside>
        ) : null}

        <section className={`panel ${showStudentFilter || showMentorFilter ? "filter-content" : ""}`}>
          <div className="inline-toolbar">
            <span className="list-total">{filteredBookings.length} total</span>
          </div>

          {feedback ? (
            <div className={`alert ${feedback.tone}`} data-testid="bookings-feedback">
              {feedback.text}
            </div>
          ) : null}
          {locationState?.flash ? (
            <div className="alert success" data-testid="bookings-flash">
              {locationState.flash}
            </div>
          ) : null}

          <div className="pill-list">
            {([
              { id: "all", label: "All" },
              { id: "reserved", label: "Reserved" },
              { id: "completed", label: "Completed" }
            ] as const).map((filter) => (
              <button
                key={filter.id}
                type="button"
                className={`filter-chip ${
                  (filter.id === "all" && isAllPresetActive) ||
                  (filter.id === "reserved" && isReservedPresetActive) ||
                  (filter.id === "completed" && isCompletedPresetActive)
                    ? "selected"
                    : ""
                }`}
                data-testid={`bookings-filter-${filter.id}`}
                onClick={() => applyQuickPreset(filter.id)}
              >
                {filter.label}
              </button>
            ))}
          </div>

          {filteredBookings.length === 0 ? (
            <div className="empty-state" data-testid="bookings-empty-state">
              <strong>{isAdmin ? "No bookings match the selected filters." : "No bookings yet."}</strong>
              <span>
                {isAdmin
                  ? "Adjust student or mentor filters to find another booking."
                  : "Reserve a slot from the catalog and your session history will appear here."}
              </span>
            </div>
          ) : (
            <div className="bookings-table-wrap" data-testid="bookings-table-wrap">
              <table className="bookings-table" data-testid="bookings-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Date &amp; Time</th>
                    <th>Student</th>
                    <th>Mentor</th>
                    <th>Price</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredBookings.map((booking) => (
                    <tr key={booking.id} data-testid={`booking-row-${booking.id}`}>
                      <td>
                        <Link
                          className="text-link"
                          data-testid={`booking-open-link-${booking.id}`}
                          to={`/bookings/${booking.publicId}`}
                        >
                          {booking.offerTitle}
                        </Link>
                      </td>
                      <td>
                        <div className="bookings-table-time">
                          <span>{new Date(booking.slotStartTime).toLocaleDateString()}</span>
                          <span className="muted">
                            {new Date(booking.slotStartTime).toLocaleTimeString([], {
                              hour: "2-digit",
                              minute: "2-digit"
                            })}{" "}
                            -{" "}
                            {new Date(booking.slotEndTime).toLocaleTimeString([], {
                              hour: "2-digit",
                              minute: "2-digit"
                            })}
                          </span>
                        </div>
                      </td>
                      <td>{booking.studentDisplayName}</td>
                      <td>{booking.mentorDisplayName}</td>
                      <td>{booking.priceCredits} credits</td>
                      <td>
                        <span className={`badge subtle status-chip status-${getStatusClass(booking.status)}`}>
                          {booking.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
