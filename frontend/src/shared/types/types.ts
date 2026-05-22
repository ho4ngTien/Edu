export interface WorkspaceFile {
  path: string;
  name: string;
  isDirectory: boolean;
  content: string;
}

export interface Objective {
  id: string;
  description: string;
  completed: boolean;
}

export interface Lesson {
  id: string;
  title: string;
  subtitle: string;
  instructions: string;
  hints: string[];
  objectives: Objective[];
  targetFile: string;
}

export interface ExecutionResult {
  stdout: string;
  stderr: string;
  exitCode: number;
  compileError: string | null;
  executionTimeMs: number;
}
