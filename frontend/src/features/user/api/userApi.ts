import axios from 'axios';
import type { UserProfile } from '../types';

const API_BASE = '/api/users';

export const userApi = {
  getMyProfile: async (): Promise<UserProfile> => {
    const response = await axios.get(`${API_BASE}/profile/me`);
    return response.data.data;
  },

  updateMyProfile: async (fullName: string, avatarUrl?: string, phone?: string): Promise<UserProfile> => {
    const response = await axios.put(`${API_BASE}/profile/me`, { fullName, avatarUrl, phone });
    return response.data.data;
  }
};
