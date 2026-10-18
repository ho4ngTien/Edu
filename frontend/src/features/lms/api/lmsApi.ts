import axios from 'axios';
import type { Assignment } from '../types';

const API_BASE = '/api/lms';

export const lmsApi = {
  createAssignment: async (classId: string, title: string, description?: string, dueDate?: string): Promise<Assignment> => {
    const response = await axios.post(`${API_BASE}/classes/${classId}/assignments`, { title, description, dueDate });
    return response.data.data;
  },
  
  getAssignments: async (classId: string): Promise<Assignment[]> => {
    const response = await axios.get(`${API_BASE}/classes/${classId}/assignments`);
    return response.data.data;
  }
};
