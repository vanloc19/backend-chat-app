/**
 * Shared utility helpers for NestJS services.
 * Go and Java services use their own internal utility packages.
 */

/** Strips null/undefined keys from an object before sending as HTTP response. */
export function stripNullish<T extends object>(obj: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(obj).filter(([, v]) => v != null),
  ) as Partial<T>;
}

/** Returns current UTC timestamp in ISO 8601 format. */
export function nowIso(): string {
  return new Date().toISOString();
}

/** Paginates an in-memory array. */
export function paginate<T>(items: T[], page: number, limit: number): T[] {
  return items.slice((page - 1) * limit, page * limit);
}

/** Sleep for a given number of milliseconds (useful in retry logic). */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
