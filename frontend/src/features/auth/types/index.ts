export interface LoginRequest {
  username: string;
  passwordHash: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  role: string;
}
