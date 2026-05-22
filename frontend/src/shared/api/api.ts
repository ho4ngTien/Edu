import type { WorkspaceFile, Lesson, Objective, ExecutionResult } from '../types/types';

const API_BASE = '/api';

// Fallback Mock Data in case backend is offline
const MOCK_FILES: WorkspaceFile[] = [
  {
    path: 'Main.java',
    name: 'Main.java',
    isDirectory: false,
    content: `public class Main {
    public static void main(String[] args) {
        System.out.println("=== Chào mừng bạn đến với EduIDE! ===");
        System.out.println("Java compiles and runs live on the Spring Boot backend.");
        
        int sum = 0;
        for (int i = 1; i <= 10; i++) {
            sum += i;
        }
        System.out.println("Tổng từ 1 đến 10 là: " + sum);
    }
}`
  },
  {
    path: 'index.html',
    name: 'index.html',
    isDirectory: false,
    content: `<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Học lập trình cùng EduIDE</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="card">
        <h1>Xin chào thế giới! 🌟</h1>
        <p id="message">Chào mừng bạn đến với bài học thiết kế giao diện Web đầu tiên.</p>
        <button id="action-btn">Nhấp vào tôi!</button>
    </div>
    <script src="script.js"></script>
</body>
</html>`
  },
  {
    path: 'style.css',
    name: 'style.css',
    isDirectory: false,
    content: `body {
    font-family: 'Outfit', sans-serif;
    background: linear-gradient(135deg, #0f172a, #1e1b4b);
    color: #f8fafc;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    margin: 0;
}

.card {
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    padding: 32px;
    text-align: center;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
    max-width: 400px;
    transition: transform 0.3s ease;
}

.card:hover {
    transform: translateY(-5px);
}

h1 {
    margin-top: 0;
    color: #6366f1;
}

button {
    background: #6366f1;
    color: white;
    border: none;
    padding: 10px 20px;
    font-size: 16px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;
}

button:hover {
    background: #4f46e5;
}`
  },
  {
    path: 'script.js',
    name: 'script.js',
    isDirectory: false,
    content: `// Viết mã JavaScript của bạn ở đây
const button = document.getElementById('action-btn');
const message = document.getElementById('message');

button.addEventListener('click', () => {
    message.textContent = 'Tuyệt vời! Bạn đã kích hoạt sự kiện click thành công! 🎉';
    button.style.backgroundColor = '#10b981';
    button.textContent = 'Thành công!';
    console.log('Button clicked and message updated successfully.');
});`
  }
];

const MOCK_LESSONS: Lesson[] = [
  {
    id: 'html-basics',
    title: 'Cấp độ 1: Cấu trúc Web (HTML)',
    subtitle: 'Tạo cấu trúc thẻ giao diện cho hộp hồ sơ người dùng',
    instructions: `<p>Chào mừng bạn đến với bài học đầu tiên! HTML là ngôn ngữ tạo dựng bộ khung xương cho mọi trang web.</p>
<h4>Nhiệm vụ của bạn:</h4>
<ul>
  <li>Mở tệp <code>index.html</code> trong trình soạn thảo.</li>
  <li>Đảm bảo có một thẻ tiêu đề chính <code>&lt;h1&gt;</code> bên trong thân trang.</li>
  <li>Đảm bảo có một phần tử <code>&lt;div class="card"&gt;</code> bọc nội dung chính.</li>
  <li>Tạo một nút bấm <code>&lt;button id="action-btn"&gt;</code> để chuẩn bị thêm tương tác.</li>
</ul>`,
    hints: [
      "Bạn có thể viết: <div class=\"card\"> <h1>Xin chào!</h1> <button id=\"action-btn\">Nhấp tôi</button> </div>",
      "Đảm bảo viết đúng chính tả class và id!"
    ],
    objectives: [
      { id: "html_heading", description: "Tạo thẻ tiêu đề chính <h1> để chào mừng người dùng", completed: false },
      { id: "html_card", description: "Tạo một thẻ div bọc ngoài có class=\"card\" để hiển thị hộp giao diện", completed: false },
      { id: "html_button", description: "Tạo một nút bấm với id=\"action-btn\"", completed: false }
    ],
    targetFile: 'index.html'
  },
  {
    id: 'css-styles',
    title: 'Cấp độ 2: Trang trí giao diện (CSS)',
    subtitle: 'Khoác áo cho bộ khung HTML với dải màu chuyển sắc và hiệu ứng bo góc',
    instructions: `<p>Trang web sẽ trông rất đơn điệu nếu không có CSS. CSS giúp tạo màu sắc, bố cục và hiệu ứng thị giác.</p>
<h4>Nhiệm vụ của bạn:</h4>
<ul>
  <li>Mở tệp <code>style.css</code> trong trình soạn thảo.</li>
  <li>Thêm thuộc tính dải màu chuyển sắc <code>linear-gradient</code> vào bộ chọn body.</li>
  <li>Áp dụng thuộc tính <code>border-radius</code> (ví dụ: 12px hoặc 16px) vào bộ chọn <code>.card</code> để tạo viền bo mềm mại.</li>
  <li>Tạo một bộ chọn hover <code>.card:hover</code> hoặc <code>button:hover</code> để thêm cảm giác phản hồi sống động!</li>
</ul>`,
    hints: [
      "Sử dụng background: linear-gradient(135deg, #0f172a, #1e1b4b);",
      "Đừng quên viết thêm .card:hover hoặc button:hover để vượt qua bài kiểm tra!"
    ],
    objectives: [
      { id: "css_background", description: "Thêm màu nền dải màu chuyển sắc (linear-gradient) cho trang", completed: false },
      { id: "css_border_radius", description: "Căn chỉnh bo tròn góc (border-radius) cho thẻ hộp hồ sơ", completed: false },
      { id: "css_hover", description: "Thêm hiệu ứng hover (:hover) để đổi màu nền hoặc bóng đổ khi rê chuột", completed: false }
    ],
    targetFile: 'style.css'
  },
  {
    id: 'js-click',
    title: 'Cấp độ 3: Tương tác người dùng (JavaScript)',
    subtitle: 'Làm cho trang web sống động với các phản hồi sự kiện bấm nút',
    instructions: `<p>JavaScript cung cấp khả năng xử lý hành vi và làm cho trang web trở nên động và thông minh.</p>
<h4>Nhiệm vụ của bạn:</h4>
<ul>
  <li>Mở tệp <code>script.js</code> trong trình soạn thảo.</li>
  <li>Thêm bộ lắng nghe sự kiện nhấp chuột <code>addEventListener('click', ...)</code> cho nút bấm có id <code>action-btn</code>.</li>
  <li>Đảm bảo ghi nhật ký thông báo ra màn hình terminal thông qua lệnh <code>console.log()</code> bên trong hàm callback click.</li>
</ul>`,
    hints: [
      "Đảm bảo lấy đúng nút bấm bằng document.getElementById('action-btn')",
      "Thêm câu lệnh console.log('Bất kỳ văn bản nào') bên trong hàm lắng nghe!"
    ],
    objectives: [
      { id: "js_listener", description: "Lắng nghe sự kiện click ('click') trên nút bấm", completed: false },
      { id: "js_console", description: "Ghi nhật ký thông điệp vào bảng điều khiển console.log", completed: false }
    ],
    targetFile: 'script.js'
  },
  {
    id: 'java-loops',
    title: 'Cấp độ 4: Cú pháp nâng cao (Java)',
    subtitle: 'Chạy cấu trúc thuật toán Java với vòng lặp và xuất dữ liệu',
    instructions: `<p>Bây giờ chúng ta sẽ chuyển sang Java trên máy chủ! Java là một ngôn ngữ lập trình mạnh mẽ, biên dịch sang bytecode.</p>
<h4>Nhiệm vụ của bạn:</h4>
<ul>
  <li>Mở tệp <code>Main.java</code> trong trình soạn thảo.</li>
  <li>Sử dụng vòng lặp <code>for</code> hoặc <code>while</code> để duyệt qua một dãy số hoặc thực hiện một tác vụ lập đi lập lại.</li>
  <li>In kết quả của vòng lặp ra đầu ra tiêu chuẩn bằng <code>System.out.println()</code>.</li>
</ul>`,
    hints: [
      "Bạn có thể dùng cú pháp: for (int i = 0; i < 5; i++) { ... }",
      "Đảm bảo gọi System.out.println(...) bên trong Main.java!"
    ],
    objectives: [
      { id: "java_loop", description: "Sử dụng cấu trúc vòng lặp (for hoặc while) trong Java", completed: false },
      { id: "java_print", description: "Xuất văn bản ra đầu ra chuẩn với System.out.println()", completed: false }
    ],
    targetFile: 'Main.java'
  }
];

// In-Memory state for fallback client mode
let localFilesState = [...MOCK_FILES];

async function handleFetch(url: string, options?: RequestInit) {
  try {
    const res = await fetch(url, options);
    if (!res.ok) {
      throw new Error(`HTTP Error: ${res.status}`);
    }
    const json = await res.json();
    // Unwrap ApiResponse if present
    if (json && typeof json.success === 'boolean' && 'data' in json) {
      if (!json.success) throw new Error(json.message);
      return json.data !== null ? json.data : { message: json.message };
    }
    return json;
  } catch (e) {
    console.warn(`Backend unreachable at ${url}. Using local fallback.`, e);
    throw e;
  }
}

export const workspaceApi = {
  getFiles: async (): Promise<WorkspaceFile[]> => {
    try {
      return await handleFetch(`${API_BASE}/workspace`);
    } catch {
      return localFilesState;
    }
  },
  
  saveFile: async (path: string, content: string): Promise<{ message: string }> => {
    try {
      return await handleFetch(`${API_BASE}/workspace/save`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ path, content })
      });
    } catch {
      localFilesState = localFilesState.map(f => f.path === path ? { ...f, content } : f);
      return { message: 'File saved locally' };
    }
  },

  createFile: async (path: string, isDirectory = false): Promise<{ message: string }> => {
    try {
      return await handleFetch(`${API_BASE}/workspace/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ path, isDirectory })
      });
    } catch {
      if (!localFilesState.some(f => f.path === path)) {
        localFilesState.push({
          path,
          name: path.split('/').pop() || path,
          isDirectory,
          content: ''
        });
      }
      return { message: 'Created successfully locally' };
    }
  },

  deleteFile: async (path: string): Promise<{ message: string }> => {
    try {
      return await handleFetch(`${API_BASE}/workspace?path=${encodeURIComponent(path)}`, {
        method: 'DELETE'
      });
    } catch {
      localFilesState = localFilesState.filter(f => !f.path.startsWith(path));
      return { message: 'Deleted successfully locally' };
    }
  },

  reset: async (): Promise<WorkspaceFile[]> => {
    try {
      return await handleFetch(`${API_BASE}/workspace/reset`, { method: 'POST' });
    } catch {
      localFilesState = [...MOCK_FILES];
      return localFilesState;
    }
  }
};

export const executionApi = {
  execute: async (language: string, code: string, path: string): Promise<ExecutionResult> => {
    try {
      return await handleFetch(`${API_BASE}/execute`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ language, code, path })
      });
    } catch {
      // Local evaluation fallback for JavaScript
      if (language.toLowerCase() === 'javascript' || language.toLowerCase() === 'js') {
        let stdout = '';
        const originalLog = console.log;
        console.log = (...args) => {
          stdout += args.join(' ') + '\n';
        };
        try {
          // eslint-disable-next-line no-eval
          const res = (0, eval)(code);
          console.log = originalLog;
          return {
            stdout: stdout + (res !== undefined ? `=> ${res}` : ''),
            stderr: '',
            exitCode: 0,
            compileError: null,
            executionTimeMs: 1
          };
        } catch (e: any) {
          console.log = originalLog;
          return {
            stdout: '',
            stderr: e.toString(),
            exitCode: 1,
            compileError: null,
            executionTimeMs: 1
          };
        }
      }
      
      // Fallback for Java when compiler backend is not running
      return {
        stdout: '',
        stderr: '[LỖI KẾT NỐI]: Không thể kết nối với Spring Boot compiler service để chạy mã Java.\n\nVui lòng khởi động backend Spring Boot (`mvn spring-boot:run`) để biên dịch và thực thi Java trực tiếp trên hệ thống của bạn!',
        exitCode: 1,
        compileError: 'Backend Spring Boot is offline.',
        executionTimeMs: 0
      };
    }
  }
};

export const curriculumApi = {
  getLessons: async (): Promise<Lesson[]> => {
    try {
      return await handleFetch(`${API_BASE}/curriculum/lessons`);
    } catch {
      return MOCK_LESSONS;
    }
  },

  verifyLesson: async (lessonId: string, workspaceFiles: WorkspaceFile[]): Promise<{
    objectives: Objective[];
    success: boolean;
    message: string;
  }> => {
    try {
      return await handleFetch(`${API_BASE}/curriculum/verify/${lessonId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(workspaceFiles)
      });
    } catch {
      // Client-side regex checking if backend is offline
      const lesson = MOCK_LESSONS.find(l => l.id === lessonId);
      if (!lesson) throw new Error('Lesson not found');
      
      const file = workspaceFiles.find(f => f.path === lesson.targetFile);
      const content = file ? file.content : '';
      
      const objectives = lesson.objectives.map((obj: Objective) => {
        let completed = false;
        switch (obj.id) {
          case "html_heading":
            completed = content.includes('<h1') && content.includes('</h1>');
            break;
          case "html_card":
            completed = content.includes('class="card"') || content.includes("class='card'");
            break;
          case "html_button":
            completed = content.includes('id="action-btn"') || content.includes("id='action-btn'");
            break;
          case "css_background":
            completed = content.includes('linear-gradient');
            break;
          case "css_border_radius":
            completed = content.includes('border-radius');
            break;
          case "css_hover":
            completed = content.includes(':hover');
            break;
          case "js_listener":
            completed = content.includes('addEventListener') && content.includes('click');
            break;
          case "js_console":
            completed = content.includes('console.log');
            break;
          case "java_loop":
            completed = content.includes('for ') || content.includes('for(') || content.includes('while ') || content.includes('while(');
            break;
          case "java_print":
            completed = content.includes('System.out.println');
            break;
        }
        return { ...obj, completed };
      });
      
      const allPassed = objectives.every((obj: Objective) => obj.completed);
      return {
        objectives,
        success: allPassed,
        message: allPassed ? "Chúc mừng! Bạn đã hoàn thành xuất sắc tất cả mục tiêu của bài học! 🎉 (Local Validation)" 
                             : "Hãy tiếp tục thử nhé! Một số mục tiêu kiểm tra vẫn chưa hoàn thành."
      };
    }
  }
};
