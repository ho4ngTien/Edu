export interface Grade {
  id: string;
  studentId: string;
  assignmentId: string;
  score: number;
  feedback?: string;
}

export interface Transcript {
  id: string;
  studentId: string;
  semester: string;
  gpa: number;
  totalCredits: number;
}
