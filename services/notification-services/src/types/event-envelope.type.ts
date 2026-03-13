export type EventEnvelope = {
  type: string;
  payload: Record<string, unknown>;
  timestamp: string;
};
