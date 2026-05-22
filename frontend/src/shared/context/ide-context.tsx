import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from 'react';
import type { WorkspaceFile, Lesson, Objective, ExecutionResult } from '../types/types';
import { workspaceApi, executionApi, curriculumApi } from '../api/api';
import type { TerminalLog } from '../../features/terminal';

// ─── State Shape ─────────────────────────────────────────────────────────────

interface IdeState {
  // Workspace
  files: WorkspaceFile[];
  openFiles: WorkspaceFile[];
  activeFile: WorkspaceFile | null;

  // Curriculum
  lessons: Lesson[];
  activeLesson: Lesson | null;
  objectives: Objective[];

  // Terminal
  logs: TerminalLog[];
  lastExecution: ExecutionResult | null;

  // UI flags
  isRunning: boolean;
  isVerifying: boolean;
  isBackendConnected: boolean;
  theme: 'dark' | 'light';
}

// ─── Action Shape ─────────────────────────────────────────────────────────────

interface IdeActions {
  // Workspace
  handleSelectFile: (file: WorkspaceFile) => void;
  handleCloseFile: (path: string) => void;
  handleCodeChange: (path: string, content: string) => Promise<void>;
  handleCreateFile: (path: string, isDirectory?: boolean) => Promise<void>;
  handleDeleteFile: (path: string) => Promise<void>;
  handleResetWorkspace: () => Promise<void>;

  // Execution
  handleRunCode: () => Promise<void>;
  handleExecuteStatement: (expr: string) => Promise<void>;

  // Curriculum
  handleSelectLesson: (lesson: Lesson) => void;
  handleVerifyLesson: () => Promise<boolean>;

  // Terminal
  addLog: (text: string, type?: TerminalLog['type']) => void;
  clearLogs: () => void;

  // UI
  toggleTheme: () => void;
}

// ─── Context ─────────────────────────────────────────────────────────────────

type IdeContextValue = IdeState & IdeActions;

const IdeContext = createContext<IdeContextValue | null>(null);

// ─── Provider ────────────────────────────────────────────────────────────────

export function IdeProvider({ children }: { children: ReactNode }) {
  // Workspace state
  const [files, setFiles] = useState<WorkspaceFile[]>([]);
  const [openFiles, setOpenFiles] = useState<WorkspaceFile[]>([]);
  const [activeFile, setActiveFile] = useState<WorkspaceFile | null>(null);

  // Curriculum state
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [activeLesson, setActiveLesson] = useState<Lesson | null>(null);
  const [objectives, setObjectives] = useState<Objective[]>([]);

  // Terminal state
  const [logs, setLogs] = useState<TerminalLog[]>([]);
  const [lastExecution, setLastExecution] = useState<ExecutionResult | null>(null);

  // UI flags
  const [isRunning, setIsRunning] = useState(false);
  const [isVerifying, setIsVerifying] = useState(false);
  const [isBackendConnected, setIsBackendConnected] = useState(false);
  const [theme, setTheme] = useState<'dark' | 'light'>('dark');

  // ── Logging helper ──────────────────────────────────────────────────────────
  const addLog = useCallback((text: string, type: TerminalLog['type'] = 'log') => {
    const time = new Date().toLocaleTimeString();
    setLogs(prev => [...prev, { type, text, time }]);
  }, []);

  const clearLogs = useCallback(() => setLogs([]), []);

  // ── Theme ───────────────────────────────────────────────────────────────────
  const toggleTheme = useCallback(() => {
    setTheme(prev => {
      const next = prev === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      return next;
    });
  }, []);

  // ── Workspace loaders ───────────────────────────────────────────────────────
  const loadWorkspace = useCallback(async (selectDefault = false) => {
    try {
      const data = await workspaceApi.getFiles();
      setFiles(data);
      setIsBackendConnected(true);

      setOpenFiles(prev => {
        if (prev.length > 0) {
          const updated = prev.map(of => data.find(f => f.path === of.path) || of);
          setActiveFile(active => {
            if (!active) return active;
            return data.find(f => f.path === active.path) || null;
          });
          return updated;
        }
        if (selectDefault && data.length > 0) {
          const firstFile = data.find(f => f.path === 'Main.java') || data[0];
          setActiveFile(firstFile);
          return [firstFile];
        }
        return prev;
      });
    } catch {
      setIsBackendConnected(false);
    }
  }, []);

  const loadLessons = useCallback(async () => {
    try {
      const data = await curriculumApi.getLessons();
      setLessons(data);
      if (data.length > 0) {
        setActiveLesson(data[0]);
        setObjectives(data[0].objectives);
      }
    } catch {
      setIsBackendConnected(false);
    }
  }, []);

  // ── Workspace handlers ──────────────────────────────────────────────────────
  const handleSelectFile = useCallback((file: WorkspaceFile) => {
    setOpenFiles(prev => prev.some(f => f.path === file.path) ? prev : [...prev, file]);
    setActiveFile(file);
  }, []);

  const handleCloseFile = useCallback((path: string) => {
    setOpenFiles(prev => {
      const remaining = prev.filter(f => f.path !== path);
      setActiveFile(active => {
        if (active && active.path === path) {
          return remaining.length > 0 ? remaining[remaining.length - 1] : null;
        }
        return active;
      });
      return remaining;
    });
  }, []);

  const handleCodeChange = useCallback(async (path: string, content: string) => {
    const updater = (f: WorkspaceFile) => f.path === path ? { ...f, content } : f;
    setFiles(prev => prev.map(updater));
    setOpenFiles(prev => prev.map(updater));
    setActiveFile(prev => prev && prev.path === path ? { ...prev, content } : prev);

    try {
      await workspaceApi.saveFile(path, content);
    } catch {
      // Saved locally in state
    }
  }, []);

  const handleCreateFile = useCallback(async (path: string, isDirectory = false) => {
    try {
      await workspaceApi.createFile(path, isDirectory);
      addLog(`[HỆ THỐNG]: Đã tạo thành công ${isDirectory ? 'thư mục' : 'tệp'} "${path}"`, 'info');
      await loadWorkspace();
      if (!isDirectory) {
        setFiles(prev => {
          const newFile = prev.find(f => f.path === path);
          if (newFile) handleSelectFile(newFile);
          return prev;
        });
      }
    } catch (e: any) {
      addLog(`[LỖI HỆ THỐNG]: Không thể tạo đối tượng: ${e.message}`, 'error');
    }
  }, [addLog, loadWorkspace, handleSelectFile]);

  const handleDeleteFile = useCallback(async (path: string) => {
    try {
      await workspaceApi.deleteFile(path);
      addLog(`[HỆ THỐNG]: Đã xóa thành công "${path}"`, 'info');
      handleCloseFile(path);
      await loadWorkspace();
    } catch (e: any) {
      addLog(`[LỖI HỆ THỐNG]: Không thể xóa đối tượng: ${e.message}`, 'error');
    }
  }, [addLog, handleCloseFile, loadWorkspace]);

  const handleResetWorkspace = useCallback(async () => {
    try {
      const data = await workspaceApi.reset();
      setFiles(data);
      setOpenFiles([]);
      setActiveFile(null);
      setLogs([]);
      setLastExecution(null);
      addLog('[HỆ THỐNG]: Đã khôi phục không gian làm việc về trạng thái mặc định ban đầu.', 'info');
      if (data.length > 0) {
        const defaultFile = data.find(f => f.path === 'Main.java') || data[0];
        setOpenFiles([defaultFile]);
        setActiveFile(defaultFile);
      }
    } catch (e: any) {
      addLog(`[LỖI HỆ THỐNG]: Không thể khôi phục không gian làm việc: ${e.message}`, 'error');
    }
  }, [addLog]);

  // ── Execution handlers ──────────────────────────────────────────────────────
  const handleRunCode = useCallback(async () => {
    setActiveFile(current => {
      if (!current) return current;
      (async () => {
        setIsRunning(true);
        addLog(`[BIÊN DỊCH]: Đang bắt đầu biên dịch và chạy tệp "${current.name}"...`, 'info');
        const ext = current.name.split('.').pop()?.toLowerCase();
        const language = ext === 'java' ? 'java' : ext === 'js' ? 'javascript' : 'text';
        try {
          const result = await executionApi.execute(language, current.content, current.path);
          setLastExecution(result);
          if (result.compileError) {
            addLog(`[LỖI BIÊN DỊCH]:\n${result.compileError}`, 'error');
          } else {
            if (result.stdout) addLog(result.stdout, 'log');
            if (result.stderr) addLog(result.stderr, 'error');
            if (result.exitCode === 0 && !result.stderr) {
              addLog(`[THÀNH CÔNG]: Thực thi tệp "${current.name}" hoàn tất thành công.`, 'success');
            } else {
              addLog(`[CẢNH BÁO]: Tiến trình kết thúc với mã thoát: ${result.exitCode}`, 'info');
            }
          }
        } catch (e: any) {
          addLog(`[LỖI THỰC THI]: ${e.message}`, 'error');
        } finally {
          setIsRunning(false);
        }
      })();
      return current;
    });
  }, [addLog]);

  const handleExecuteStatement = useCallback(async (expr: string) => {
    addLog(`> ${expr}`, 'log');
    try {
      const result = await executionApi.execute('javascript', expr, '');
      if (result.stdout) addLog(result.stdout, 'log');
      if (result.stderr) addLog(result.stderr, 'error');
    } catch (e: any) {
      addLog(`Error: ${e.message}`, 'error');
    }
  }, [addLog]);

  // ── Curriculum handlers ─────────────────────────────────────────────────────
  const handleSelectLesson = useCallback((lesson: Lesson) => {
    setActiveLesson(lesson);
    setObjectives(lesson.objectives);
    setFiles(prev => {
      const targetFile = prev.find(f => f.path === lesson.targetFile);
      if (targetFile) handleSelectFile(targetFile);
      return prev;
    });
    addLog(`[HỆ THỐNG]: Đã chuyển sang bài học "${lesson.title}"`, 'info');
  }, [addLog, handleSelectFile]);

  const handleVerifyLesson = useCallback(async (): Promise<boolean> => {
    if (!activeLesson) return false;
    setIsVerifying(true);
    addLog(`[KIỂM TRA]: Đang kiểm tra bài nộp cho cấp độ "${activeLesson.title}"...`, 'info');
    try {
      const result = await curriculumApi.verifyLesson(activeLesson.id, files);
      setObjectives(result.objectives);
      if (result.success) {
        addLog(`[MỤC TIÊU]: ${result.message}`, 'success');
        return true;
      } else {
        addLog('[MỤC TIÊU]: Một số mục tiêu kiểm tra vẫn chưa hoàn thành. Xem bảng điều khiển Hướng dẫn để giải bài!', 'error');
        return false;
      }
    } catch (e: any) {
      addLog(`[KIỂM TRA LỖI]: ${e.message}`, 'error');
      return false;
    } finally {
      setIsVerifying(false);
    }
  }, [activeLesson, files, addLog]);

  // ── Bootstrap ───────────────────────────────────────────────────────────────
  useEffect(() => {
    const initialize = async () => {
      await loadWorkspace(true);
      await loadLessons();
      addLog('[HỆ THỐNG]: Chào mừng bạn đến với EduIDE! Môi trường phát triển đã sẵn sàng.', 'success');
    };
    initialize();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ── Context value ───────────────────────────────────────────────────────────
  const value: IdeContextValue = {
    // State
    files, openFiles, activeFile,
    lessons, activeLesson, objectives,
    logs, lastExecution,
    isRunning, isVerifying, isBackendConnected, theme,

    // Actions
    handleSelectFile, handleCloseFile, handleCodeChange,
    handleCreateFile, handleDeleteFile, handleResetWorkspace,
    handleRunCode, handleExecuteStatement,
    handleSelectLesson, handleVerifyLesson,
    addLog, clearLogs, toggleTheme,
  };

  return <IdeContext.Provider value={value}>{children}</IdeContext.Provider>;
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export function useIde(): IdeContextValue {
  const ctx = useContext(IdeContext);
  if (!ctx) throw new Error('useIde must be used within an <IdeProvider>');
  return ctx;
}
