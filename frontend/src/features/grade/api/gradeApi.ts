import axios from 'axios';
import type { Grade } from '../types';

const API_BASE = '/api/grade';

export const gradeApi = {
  submitGrade: async (studentId: string, assignmentId: string, score: number, feedback?: string): Promise<Grade> => {
    const response = await axios.post(`${API_BASE}/submit`, { studentId, assignmentId, score, feedback });
    return response.data.data;
  },
  
  getStudentGrades: async (studentId: string): Promise<Grade[]> => {
    const response = await axios.get(`${API_BASE}/students/${studentId}`);
    return response.data.data;
  }
};
