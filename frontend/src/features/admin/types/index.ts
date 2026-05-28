export interface SystemConfig {
  id: string;
  value: string;
  description?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface Report {
  id: string;
  name: string;
  type: string;
  data: string;
  generatedAt?: string;
  generatedBy?: string;
}
