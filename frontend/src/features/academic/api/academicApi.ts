import axios from 'axios';
import type { Course, AcademicClass } from '../types';

const API_BASE = '/api/academic';

export const academicApi = {
  getAllCourses: async (): Promise<Course[]> => {
    const response = await axios.get(`${API_BASE}/courses`);
    return response.data.data;
  },
  
  getAllClasses: async (): Promise<AcademicClass[]> => {
    const response = await axios.get(`${API_BASE}/classes`);
    return response.data.data;
  }
};
