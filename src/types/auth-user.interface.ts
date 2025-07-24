export interface AuthUser {
  userId: number;
  sessionId: number;
  role: string;
}

export interface RefreshUser {
  sessionId: number;
}