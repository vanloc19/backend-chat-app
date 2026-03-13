import type { FriendRequestPayload } from './friend-request-payload.type.js';

export type FriendRequestedEvent = {
  type: 'friend.requested';
  payload: FriendRequestPayload;
  timestamp: string;
};
