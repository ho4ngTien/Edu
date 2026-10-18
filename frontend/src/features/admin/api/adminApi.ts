import axios from 'axios';
import type { SystemConfig } from '../types';

const API_BASE = '/api/admin';

export const adminApi = {
  getConfig: async (configId: string): Promise<SystemConfig> => {
    const response = await axios.get(`${API_BASE}/configs/${configId}`);
    return response.data.data;
  },
  
  updateConfig: async (configId: string, value: string, description: string): Promise<SystemConfig> => {
    const response = await axios.put(`${API_BASE}/configs/${configId}`, { value, description });
    return response.data.data;
  }
};
