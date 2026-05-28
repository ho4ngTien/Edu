export interface Course {
  id: string;
  code: string;
  name: string;
  credits: number;
  description?: string;
  isActive: boolean;
}

export interface AcademicClass {
  id: string;
  courseId: string;
  name: string;
  semester: string;
  maxCapacity: number;
}
