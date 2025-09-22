// src/utils/request.ts
// @ts-nocheck

// 配置基础地址
const baseURL = 'http://localhost:8095'; // 替换为你的后端地址

// 请求拦截器：添加 token、统一配置等
const requestInterceptor = (options) => {
  // 1. 添加 baseURL（仅当传入相对路径时）
  if (options.url && !/^https?:\/\//i.test(options.url)) {
    if (options.url.startsWith('/')) {
      options.url = baseURL + options.url;
    } else {
      options.url = `${baseURL}/${options.url}`;
    }
  }
  // 2. 添加请求头（如 token）
  const token = uni.getStorageSync('token'); // 从缓存获取 token
  if (token) {
    options.header = {
      ...options.header,
      'Authorization': `Bearer ${token}`, // 根据后端要求调整格式
    };
  }
  // 3. 统一设置 Content-Type
  options.header = {
    'Content-Type': 'application/json',
    ...options.header,
  };
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
  // 常见结构 A: { code, message, data }
  // 常见结构 B: 直接返回对象/数组/字符串/数字
  if (data && typeof data === 'object' && 'code' in data) {
    // 按结构 A 处理
    if ((data as any).code !== 200) {
      uni.showToast({ title: (data as any).message || '操作失败', icon: 'none' });
      return Promise.reject(new Error((data as any).message || 'Backend Error'));
    }
    return Promise.resolve((data as any).data);
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
        uni.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      },
    });
  });
};

// 封装 GET/POST 等快捷方法（简化调用）
export const http = {
  get: (url, params, options) => 
    request({ ...options, url, method: 'GET', data: params }),
  
  post: (url, data, options) => 
    request({ ...options, url, method: 'POST', data }),
  
  put: (url, data, options) => 
    request({ ...options, url, method: 'PUT', data }),
  
  delete: (url, params, options) => 
    request({ ...options, url, method: 'DELETE', data: params }),
};

export default http;