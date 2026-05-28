export interface UserProfile {
  id: string;
  authUserId: string;
  fullName: string;
  avatarUrl?: string;
  phone?: string;
}

export interface Student {
  id: string;
  profileId: string;
  studentCode: string;
  admissionDate?: string;
}

export interface Teacher {
  id: string;
  profileId: string;
  teacherCode: string;
  department?: string;
  hireDate?: string;
}
