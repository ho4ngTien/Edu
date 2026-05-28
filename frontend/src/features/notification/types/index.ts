export interface Notification {
  id: string;
  userId: string;
  title: string;
  content: string;
  isRead: boolean;
  type: 'SYSTEM' | 'DEADLINE' | 'GRADE';
  createdAt: string;
}
