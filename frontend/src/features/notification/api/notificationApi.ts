import axios from 'axios';
import type { Notification } from '../types';

const API_BASE = '/api/notifications';

export const notificationApi = {
  getMyNotifications: async (): Promise<Notification[]> => {
    const response = await axios.get(`${API_BASE}/my-notifications`);
    return response.data.data;
  },

  markAsRead: async (id: string): Promise<Notification> => {
    const response = await axios.put(`${API_BASE}/${id}/read`);
    return response.data.data;
  }
};
