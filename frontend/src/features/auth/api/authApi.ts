import axios from 'axios';
import { LoginRequest, AuthResponse } from '../types';

const API_BASE = '/api/auth';

export const authApi = {
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    const response = await axios.post(`${API_BASE}/login`, request);
    return response.data.data;
  },
  
  logout: async (): Promise<void> => {
    await axios.post(`${API_BASE}/logout`);
  }
};
