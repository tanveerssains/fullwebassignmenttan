export type EmploymentStatus = 'ACTIVE' | 'ON_LEAVE' | 'INACTIVE';

export interface Department {
  id: number;
  name: string;
  location?: string;
}

export interface Project {
  id: number;
  name: string;
  client?: string;
  status?: string;
}

export interface Employee {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone?: string | null;
  designation: string;
  salary: number;
  dateOfJoining: string;
  status: EmploymentStatus;
  department: Department;
  project?: Project | null;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeRequest {
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string | null;
  designation: string;
  salary: number;
  dateOfJoining: string;
  status: EmploymentStatus;
  departmentId: number;
  projectId?: number | null;
}

export interface ApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}
