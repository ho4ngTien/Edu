export type TerminalLog = {
  type: 'log' | 'info' | 'error' | 'warn' | 'success';
  text: string;
  time: string;
};
