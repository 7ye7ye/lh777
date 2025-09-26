// src/utils/request.ts
// @ts-nocheck

// 配置基础地址：支持运行时覆盖
const detectBaseURL = (): string => {
  // 1) 运行时存储覆盖（在微信开发者工具控制台可设置：uni.setStorageSync('BASE_URL', 'http://ip:port')）
  const stored = typeof uni !== 'undefined' ? uni.getStorageSync('BASE_URL') : '';
  if (stored) return stored;

  // 2) 全局变量覆盖（在页面注入 window.__API_BASE_URL / globalThis.__API_BASE_URL）
  const g: any = (typeof globalThis !== 'undefined' ? globalThis : (typeof window !== 'undefined' ? window : {}));
  if (g && g.__API_BASE_URL) return g.__API_BASE_URL as string;

  // 3) 默认值（本地开发）
  return 'http://localhost:8095';
};

const baseURL = detectBaseURL();
const API_PREFIX = '/jeecg-boot';

// 请求拦截器：添加 token、统一配置等
// 扩展可用字段：
// - params: GET/DELETE 查询参数对象
// - contentType: 'json' | 'form' | 'multipart'
// - baseURL: 覆盖默认 baseURL
// - skipAuth: 不携带 Authorization 头
// - silent: 失败不自动 toast
const requestInterceptor = (options) => {
  // 1. 添加 baseURL（仅当传入相对路径时）
  if (options.url && !/^https?:\/\//i.test(options.url)) {
    let path = options.url;
    // 确保以 / 开头
    if (!path.startsWith('/')) path = `/${path}`;
    // 统一加 jeecg-boot 前缀（避免重复）
    if (!path.startsWith(API_PREFIX + '/')) {
      path = API_PREFIX + path;
    }
    options.url = baseURL + path;
  }
  // 2. params 合并到 URL（GET/DELETE）
  const method = (options.method || 'GET').toUpperCase();
  if (options.params && (method === 'GET' || method === 'DELETE')) {
    const toUrlEncoded = (obj = {}) => Object.keys(obj)
      .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(obj[k] ?? '')}`)
      .join('&');
    const query = toUrlEncoded(options.params);
    if (query) {
      options.url += (options.url.includes('?') ? '&' : '?') + query;
    }
    delete options.params;
  }

  // 3. Content-Type 与数据体
  const header = { ...options.header };
  const contentType = options.contentType || 'json';
  if (contentType === 'json') {
    header['Content-Type'] = 'application/json';
  } else if (contentType === 'form') {
    header['Content-Type'] = 'application/x-www-form-urlencoded';
    if (options.data && typeof options.data === 'object') {
      const toUrlEncoded = (obj = {}) => Object.keys(obj)
        .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(obj[k] ?? '')}`)
        .join('&');
      options.data = toUrlEncoded(options.data);
    }
  } // multipart 由 uni 处理 boundary

  // 4. 添加请求头（如 token）
  const skipAuth = !!options.skipAuth;
  const token = uni.getStorageSync('token'); // 从缓存获取 token
  if (!skipAuth && token) {
    header['Authorization'] = `Bearer ${token}`; // 如需改为 token 直传，请在此调整
  }
  options.header = header;
  return options;
};

// 响应拦截器：统一处理错误、格式化数据
const responseInterceptor = (response) => {
  const { data, statusCode } = response;
  
  // 1. 处理 HTTP 错误（如 404、500）
  if (statusCode < 200 || statusCode >= 300) {
    uni.showToast({ title: `请求失败: ${statusCode}`, icon: 'none' });
    return Promise.reject(new Error(`HTTP Error: ${statusCode}`));
  }
  
  // 2. 处理后端自定义错误（兼容多种返回结构）
  // 常见结构 A: { code, message, data, description? }
  // 常见结构 B: { success, message, data }
  // 常见结构 C: 直接返回对象/数组/字符串/数字
  if (data && typeof data === 'object' && ('code' in data || 'success' in data)) {
    const code = data.code;
    const success = data.success;
    const ok = success === true || code === 200 || code === 0;
    if (!ok) {
      // 优先显示 description，没有则显示 message
      const errorMsg = data.description || data.message || '操作失败';
      uni.showToast({ title: errorMsg, icon: 'none' });
      return Promise.reject(new Error(errorMsg));
    }
    return Promise.resolve(data.data !== undefined ? data.data : data);
  }

  // 3. 其他结构：直接返回原始 data
  return Promise.resolve(data);
};

// 封装统一请求方法
export const request = (options) => {
  // 应用请求拦截器
  const finalOptions = requestInterceptor(options);
  
  return new Promise((resolve, reject) => {
    uni.request({
      ...finalOptions,
      // 发起请求
      success: (res) => {
        // 应用响应拦截器
        responseInterceptor(res).then(resolve).catch(reject);
      },
      // 处理网络错误（如断网）
      fail: (err) => {
        if (!finalOptions.silent) {
          uni.showToast({ title: '网络连接失败', icon: 'none' });
        }
        reject(err);
      },
    });
  });
};

// 封装 GET/POST 等快捷方法（简化调用）
export const http = {
  get: (url, params, options) => 
    request({ ...options, url, method: 'GET', params }),
  
  post: (url, data, options) => 
    request({ contentType: 'json', ...options, url, method: 'POST', data }),
  
  put: (url, data, options) => 
    request({ contentType: 'json', ...options, url, method: 'PUT', data }),
  
  delete: (url, params, options) => 
    request({ ...options, url, method: 'DELETE', params }),

  postForm: (url, data, options) =>
    request({ contentType: 'form', ...options, url, method: 'POST', data }),
};

export default http;