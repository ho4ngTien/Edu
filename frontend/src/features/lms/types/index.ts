export interface Material {
  id: string;
  classId: string;
  title: string;
  description?: string;
  fileUrl?: string;
  uploadedBy: string;
}

export interface Assignment {
  id: string;
  classId: string;
  title: string;
  description?: string;
  dueDate?: string;
  createdBy: string;
}

export interface Submission {
  id: string;
  assignmentId: string;
  studentId: string;
  content?: string;
  fileUrl?: string;
  submittedAt?: string;
}
