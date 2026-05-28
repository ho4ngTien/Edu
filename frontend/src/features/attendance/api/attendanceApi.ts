import axios from 'axios';
import { Attendance } from '../types';

const API_BASE = '/api/attendance';

export const attendanceApi = {
  recordAttendance: async (classId: string, studentId: string, date: string, status: string, remarks?: string): Promise<Attendance> => {
    const response = await axios.post(`${API_BASE}/classes/${classId}`, { studentId, date, status, remarks });
    return response.data.data;
  },
  
  getStudentAttendance: async (studentId: string): Promise<Attendance[]> => {
    const response = await axios.get(`${API_BASE}/students/${studentId}`);
    return response.data.data;
  }
};
