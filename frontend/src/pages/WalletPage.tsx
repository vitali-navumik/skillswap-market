import { FormEvent, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, ApiError } from "../lib/api";
import { getFullName } from "../lib/userNames";
import type { AuthUser, Wallet, WalletTransaction } from "../types";

interface WalletPageProps {
  token: string | null;
  user: AuthUser | null;
}

export function WalletPage({ token, user }: WalletPageProps) {
  const navigate = useNavigate();
  const params = useParams<{ walletPublicId: string }>();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [transactions, setTransactions] = useState<WalletTransaction[]>([]);
  const [adminUsers, setAdminUsers] = useState<AuthUser[]>([]);
  const [selectedStudentId, setSelectedStudentId] = useState<number | "">("");
  const [amount, setAmount] = useState(50);
  const [message, setMessage] = useState<{ tone: "success" | "error"; text: string } | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const quickTopUps = [25, 50, 100, 200];
  const isAdmin = user?.roles.includes("ADMIN") ?? false;
  const hasStudentRole = user?.roles.includes("STUDENT") ?? false;
  const hasMentorRole = user?.roles.includes("MENTOR") ?? false;
  const isMentorWalletView = hasMentorRole && !isAdmin;

  const transactionLabels: Record<WalletTransaction["type"], string> = {
    TOP_UP: "Manual top-up",
    CHARGE: "Charged for booking",
    REFUND: "Refund issued",
    PAYOUT: "Mentor payout",
    ADJUSTMENT: "Payout reversed"
  };

  const eligibleWalletUsers = useMemo(
    () =>
      adminUsers.filter(
        (entry) =>
          Boolean(entry.walletPublicId) &&
          (entry.roles.includes("STUDENT") || entry.roles.includes("MENTOR"))
      ),
    [adminUsers]
  );
  const routeWalletPublicId = params.walletPublicId ?? "";
  const selectedUser = eligibleWalletUsers.find((entry) => entry.id === selectedStudentId) ?? null;
  const selectedUserHasStudentRole = selectedUser?.roles.includes("STUDENT") ?? false;
  const selectedUserIsMentor = selectedUser?.roles.includes("MENTOR") ?? false;

  useEffect(() => {
    const load = async () => {
      if (!token) {
        return;
      }

      setLoading(true);
      setMessage(null);

      try {
        if (isAdmin) {
          const users = await api.listUsers(token);
          setAdminUsers(users);
        } else {
          const requestedWalletPublicId = routeWalletPublicId || user!.walletPublicId || "";
          if (!requestedWalletPublicId) {
            throw new ApiError(404, "Wallet route is not available for this user");
          }
          const [walletResult, transactionResult] = await Promise.all([
            api.getWallet(token, requestedWalletPublicId),
            api.getWalletTransactions(token, requestedWalletPublicId)
          ]);
          setWallet(walletResult);
          setTransactions(transactionResult);
        }
      } catch (err) {
        setMessage({
          tone: "error",
          text: err instanceof ApiError ? err.message : "Failed to load wallet"
        });
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [isAdmin, routeWalletPublicId, token, user]);

  useEffect(() => {
    if (!isAdmin) {
      return;
    }

    const nextSelectedStudentId =
      eligibleWalletUsers.find((entry) => entry.walletPublicId === routeWalletPublicId)?.id ?? "";

    setSelectedStudentId(nextSelectedStudentId);

    if (!routeWalletPublicId) {
      setWallet(null);
      setTransactions([]);
      setLoading(false);
    }
  }, [eligibleWalletUsers, isAdmin, routeWalletPublicId]);

  useEffect(() => {
    const loadSelectedWallet = async () => {
      if (!token || !isAdmin || !routeWalletPublicId || selectedStudentId === "" || !selectedUser) {
        return;
      }

      setLoading(true);
      setMessage(null);

      try {
        const [walletResult, transactionResult] = await Promise.all([
          api.getWallet(token, selectedUser.walletPublicId!),
          api.getWalletTransactions(token, selectedUser.walletPublicId!)
        ]);
        setWallet(walletResult);
        setTransactions(transactionResult);
      } catch (err) {
        setMessage({
          tone: "error",
          text: err instanceof ApiError ? err.message : "Failed to load selected user wallet"
        });
      } finally {
        setLoading(false);
      }
    };

    void loadSelectedWallet();
  }, [isAdmin, routeWalletPublicId, selectedStudentId, selectedUser, token]);
  const showTopUpPanel = isAdmin ? selectedUserHasStudentRole : hasStudentRole;

  const onTopUp = async (event: FormEvent) => {
    event.preventDefault();
    if (!token) {
      return;
    }
    setSubmitting(true);
    setMessage(null);

    try {
      if (isAdmin) {
        if (selectedStudentId === "") {
          return;
        }
        await api.topUpWallet(token, selectedUser!.walletPublicId!, amount);
        const name = selectedUser ? getFullName(selectedUser.firstName, selectedUser.lastName, selectedUser.displayName) : `User #${selectedStudentId}`;
        setMessage({ tone: "success", text: `Wallet topped up by ${amount} credits for ${name}.` });
        const [walletResult, transactionResult] = await Promise.all([
          api.getWallet(token, selectedUser!.walletPublicId!),
          api.getWalletTransactions(token, selectedUser!.walletPublicId!)
        ]);
        setWallet(walletResult);
        setTransactions(transactionResult);
      } else {
        await api.topUpWallet(token, user!.walletPublicId!, amount);
        setMessage({ tone: "success", text: `Wallet topped up by ${amount} credits.` });
        const [walletResult, transactionResult] = await Promise.all([
          api.getWallet(token, user!.walletPublicId!),
          api.getWalletTransactions(token, user!.walletPublicId!)
        ]);
        setWallet(walletResult);
        setTransactions(transactionResult);
      }
    } catch (err) {
      setMessage({
        tone: "error",
        text: err instanceof ApiError ? err.message : "Top-up failed"
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (!token) {
    return <div className="panel">Please log in to view your wallet.</div>;
  }

  if (isAdmin && !loading && eligibleWalletUsers.length === 0) {
    return (
      <div className="panel">
        <div className="empty-state" data-testid="wallet-admin-empty-state">
          <strong>No wallets available.</strong>
          <span>Selecting wallets requires at least one user with an existing wallet.</span>
        </div>
      </div>
    );
  }

  if (loading && !wallet) {
    return <div className="panel">Loading wallet...</div>;
  }

  return (
    <div className={`stack gap-xl ${isAdmin ? "list-page-shell" : "detail-shell"}`}>
      <div className={isAdmin ? "filter-layout" : "stack"}>
        {isAdmin ? (
          <aside className="panel filter-sidebar" data-testid="wallet-admin-filter-panel">
            <div className="stack gap-md">
              <div className="sidebar-heading">
                <strong>Filters</strong>
              </div>
              <label>
                User
                <select
                  data-testid="wallet-admin-student-filter"
                  value={selectedStudentId}
                  onChange={(event) => {
                    if (!event.target.value) {
                      navigate("/wallets");
                      return;
                    }

                    const nextUser = eligibleWalletUsers.find((entry) => entry.id === Number(event.target.value));
                    if (nextUser?.walletPublicId) {
                      navigate(`/wallets/${nextUser.walletPublicId}`);
                    }
                  }}
                >
                  <option value="">Choose user</option>
                  {eligibleWalletUsers.map((entry) => (
                    <option key={entry.id} value={entry.id}>
                      {getFullName(entry.firstName, entry.lastName, entry.displayName)}
                    </option>
                  ))}
                </select>
              </label>
              {selectedUser ? (
                <small className="inline-note">
                  Viewing wallet for {getFullName(selectedUser.firstName, selectedUser.lastName, selectedUser.displayName)}.
                </small>
              ) : null}
            </div>
          </aside>
        ) : null}

        <div className={isAdmin ? "filter-content" : "stack gap-xl"}>
      {isAdmin && !selectedUser ? (
        <section className="panel">
          <div className="empty-state" data-testid="wallet-admin-selection-empty-state">
            <strong>No wallet selected.</strong>
            <span>Choose a user in the filter to open a user wallet.</span>
          </div>
        </section>
      ) : (
        <>
          <section className="panel compact-stats-bar" data-testid="wallet-summary">
            <span className="compact-stat-item" data-testid="wallet-total-balance">
              <strong>Balance:</strong> {wallet?.balance ?? 0}
            </span>
          </section>

          {showTopUpPanel ? (
        <section className="panel stack gap-md" data-testid="wallet-topup-panel">
          <div className="section-heading">
            <h2>{isAdmin ? "Top up selected wallet" : "Top up with test credits"}</h2>
            <span>{isAdmin ? "Admin helper action" : "MVP helper action"}</span>
          </div>

          <div className="pill-list">
            {quickTopUps.map((quickAmount) => (
              <button
                key={quickAmount}
                type="button"
                className={`filter-chip ${amount === quickAmount ? "selected" : ""}`}
                data-testid={`wallet-quick-topup-${quickAmount}`}
                onClick={() => setAmount(quickAmount)}
              >
                +{quickAmount}
              </button>
            ))}
          </div>

          <form className="topup-row" onSubmit={onTopUp}>
            <input
              data-testid="wallet-topup-amount-input"
              type="number"
              min={1}
              value={amount}
              onChange={(event) => setAmount(Number(event.target.value))}
            />
            <button
              className="primary-button"
              type="submit"
              disabled={submitting || (isAdmin && selectedStudentId === "")}
              data-testid="wallet-topup-submit"
            >
              {submitting ? "Applying..." : "Top Up"}
            </button>
          </form>

          {message ? (
            <div className={`alert ${message.tone}`} data-testid="wallet-feedback">
              {message.text}
            </div>
          ) : null}
        </section>
          ) : (
        <section className="panel stack gap-md" data-testid="wallet-readonly-panel">
          <div className="section-heading">
            <h2>Earnings and transactions</h2>
            <span>Read-only mentor view</span>
          </div>
          <p className="inline-note">
            Mentor wallets grow from completed bookings. If a completed booking is cancelled, the payout is reversed automatically.
          </p>
          {message ? (
            <div className={`alert ${message.tone}`} data-testid="wallet-feedback">
              {message.text}
            </div>
          ) : null}
        </section>
          )}

          <section className="panel" data-testid="wallet-transactions-panel">
        <div className="section-heading">
          <h2>Transactions</h2>
          <span>{transactions.length} records</span>
        </div>

        {transactions.length === 0 ? (
          <div className="empty-state" data-testid="wallet-transactions-empty-state">
            <strong>No transactions yet.</strong>
            <span>
              {showTopUpPanel
                ? "Top up the wallet or complete a booking to populate the ledger."
                : "Complete a booking or receive a payout to populate the ledger."}
            </span>
          </div>
        ) : (
          <div className="transaction-list transaction-scroll-list" data-testid="wallet-transactions-list">
            {transactions.map((transaction) => (
              <div className="transaction-card" key={transaction.id} data-testid={`wallet-transaction-${transaction.id}`}>
                <strong>{transactionLabels[transaction.type]}</strong>
                <span
                  className={`transaction-amount ${
                    transaction.type === "PAYOUT" ||
                    transaction.type === "TOP_UP" ||
                    transaction.type === "REFUND" ||
                    (transaction.type === "ADJUSTMENT" && transaction.amount > 0)
                      ? "positive"
                      : ""
                  }`}
                >
                  {transaction.type === "PAYOUT" ||
                  transaction.type === "TOP_UP" ||
                  transaction.type === "REFUND" ||
                  (transaction.type === "ADJUSTMENT" && transaction.amount > 0)
                    ? "+"
                    : transaction.type === "CHARGE" || (transaction.type === "ADJUSTMENT" && transaction.amount < 0)
                      ? "-"
                    : ""}
                  {Math.abs(transaction.amount)} credits
                </span>
                <small className="transaction-meta">
                  {transaction.type} {"\u2022"} {new Date(transaction.createdAt).toLocaleString(undefined, {
                    month: "short",
                    day: "numeric",
                    year: "numeric",
                    hour: "numeric",
                    minute: "2-digit"
                  })}
                </small>
              </div>
            ))}
          </div>
        )}
          </section>
        </>
      )}
        </div>
      </div>
    </div>
  );
}
