export function formatRating(value: number): string {
  return `${value.toFixed(1)}/5.0`;
}

export function formatRatingValue(value: number): string {
  return value.toFixed(1);
}

export function formatRatingOutOfFive(value: number): string {
  return `${value.toFixed(1)} out of 5`;
}

export function formatReviewTimestamp(createdAt: string, updatedAt: string): string {
  const hasSeparateUpdate = updatedAt !== createdAt;
  const value = hasSeparateUpdate ? updatedAt : createdAt;
  return `${hasSeparateUpdate ? "Updated" : "Created"} ${new Date(value).toLocaleString()}`;
}
