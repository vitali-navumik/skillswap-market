export function getFullName(firstName?: string | null, lastName?: string | null, fallback?: string | null): string {
  const fullName = [firstName?.trim(), lastName?.trim()].filter(Boolean).join(" ").trim();
  return fullName || fallback?.trim() || "";
}
