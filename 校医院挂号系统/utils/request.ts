// src/utils/request.ts
// @ts-nocheck

// 导入统一配置
import { getBaseURL, getApiPrefix } from '@/config/api'

// 配置基础地址：支持运行时覆盖
const detectBaseURL = (): string => {
  // 使用统一配置
  const url = getBaseURL();
  console.log('🌐 当前 API Base URL:', url);
  return url;
};

// 在模块加载时立即获取配置（确保使用最新值）
const baseURL = detectBaseURL();
const API_PREFIX = getApiPrefix();
console.log('🌐 当前 API Prefix:', API_PREFIX);

// 请求拦截器：添加 token、统一配置等
const requestInterceptor = (options) => {
  // 1. 添加 baseURL（仅当传入相对路径时）
  if (options.url && !/^https?:\/\//i.test(options.url)) {
    let path = options.url;
    if (!path.startsWith('/')) path = `/${path}`;
    // 允许通过 noPrefix 关闭 jeecg-boot 前缀
    const usePrefix = API_PREFIX && !options?.noPrefix;
    if (usePrefix) {
      if (!path.startsWith(API_PREFIX + '/')) {
        path = API_PREFIX + path;
      }
    }
    options.url = baseURL + path;
  }
  console.log('最终请求 URL:', options.url);

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
  console.log('请求拦截器 - skipAuth:', skipAuth, 'token:', token ? token.substring(0, 20) + '...' : 'null');
  if (!skipAuth && token) {
    header['Authorization'] = `Bearer ${token}`;
    header['X-Access-Token'] = token;
    console.log('请求拦截器 - 已添加token到请求头');
  } else if (!skipAuth) {
    console.warn('请求拦截器 - 未找到token，请求可能失败');
  }
  options.header = header;
  console.log('请求拦截器 - 最终请求头:', Object.keys(header));
  return options;
};

// 响应拦截器：统一处理错误、格式化数据
const responseInterceptor = (response) => {
  const { data, statusCode } = response;
  
  console.log('响应拦截器 - 原始响应:', response);
  console.log('响应拦截器 - data:', data);
  
  // 1. 先处理后端自定义错误结构（即使HTTP状态码是错误码，也要先尝试解析响应体）
  // 常见结构 A: { code, message, data, description? }
  // 常见结构 B: { success, message, data }
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
    
    // 统一解包 JEECG 的 result
    const payload = data.result !== undefined
      ? data.result
      : (data.data !== undefined ? data.data : data);
    return Promise.resolve(payload);
  }
  
  // 2. 处理 HTTP 错误（如 404、500）- 只有在响应体中没有错误信息时才使用默认错误信息
  if (statusCode < 200 || statusCode >= 300) {
    // 检测是否是 cpolar 404 错误（cpolar 隧道地址失效）
    const isCpolar404 = statusCode === 404 && 
      typeof data === 'string' && 
      (data.includes('cpolar.com') || data.includes('The page you were looking for domain doesn\'t exist'));
    
    const httpMsg = isCpolar404
      ? 'cpolar 隧道地址已失效，请重新获取新地址并更新 config/api.ts'
      : statusCode === 502
      ? '网关错误(502)：后端服务不可达或路由未配置'
      : statusCode === 404
      ? '接口不存在(404)'
      : statusCode >= 500
      ? '服务器错误'
      : `请求失败: ${statusCode}`;
    
    // 对于 cpolar 404 错误，显示更详细的提示
    if (isCpolar404) {
      console.error('❌ cpolar 隧道地址失效！');
      console.error('💡 解决步骤：');
      console.error('   1. 打开 cpolar 客户端');
      console.error('   2. 查看 "在线隧道列表" 中的 HTTPS 地址');
      console.error('   3. 更新 config/api.ts 中的 BASE_URL');
      console.error('   4. 重新编译项目');
      uni.showModal({
        title: 'cpolar 隧道地址失效',
        content: '请重新获取新的 cpolar 地址并更新 config/api.ts 中的 BASE_URL',
        showCancel: false,
        confirmText: '知道了'
      });
    } else {
      uni.showToast({ title: httpMsg, icon: 'none' });
    }
    return Promise.reject(new Error(httpMsg));
  }

  // 3. 处理Spring Boot ResponseEntity响应格式
  // ResponseEntity.ok(data) 的响应结构通常是 { body: actualData, statusCode: 200, ... }
  if (data && typeof data === 'object' && data.body !== undefined) {
    console.log('检测到ResponseEntity格式，提取body:', data.body);
    return Promise.resolve(data.body);
  }

  // 4. 其他结构：直接返回原始 data
  console.log('返回原始data:', data);
  return Promise.resolve(data);
}

// 封装统一请求方法
export const request = (options) => {
  // 应用请求拦截器
  const finalOptions = requestInterceptor(options);
  
  return new Promise((resolve, reject) => {
    uni.request({
      ...finalOptions,
      timeout: finalOptions.timeout ?? 8000,
      // 发起请求
      success: (res) => {
        // 应用响应拦截器
        responseInterceptor(res).then(resolve).catch(reject);
      },
      // 处理网络错误（如断网/空响应）
      fail: (err) => {
        if (!finalOptions.silent) {
          const msg =
            typeof err?.errMsg === 'string' && err.errMsg.includes('ERR_EMPTY_RESPONSE')
              ? '服务器未返回数据（可能端口/HTTPS/防火墙/网关导致）'
              : '网络连接失败';
          uni.showToast({ title: msg, icon: 'none' });
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

  // 上传文件：走同一套拦截器逻辑，自动拼接 baseURL / API_PREFIX，并带上 token
  upload: (url, filePath, extra = {}) => {
    // 复用 requestInterceptor 来拼接 URL 和 Header（含 token）
    const finalOptions = requestInterceptor({
      url,
      // 上传文件通常是 multipart/form-data，统一用 form，让拦截器不要强制 json
      contentType: 'multipart',
      header: extra.header || {},
      skipAuth: extra.skipAuth,
      noPrefix: extra.noPrefix,
    });

    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: finalOptions.url,
        filePath,
        name: extra.name || 'file',
        formData: extra.formData || {},
        header: finalOptions.header,
        timeout: finalOptions.timeout ?? 8000,
        success: (uploadRes) => {
          // uploadFile 的 res.data 一般是字符串，这里交给 responseInterceptor 统一处理
          // 构造一个类似 uni.request 的响应结构
          let data: any = uploadRes.data;
          try {
            if (typeof data === 'string') {
              data = JSON.parse(data);
            }
          } catch (_) {
            // 如果不是合法 JSON，就按原样丢给响应拦截器，那里会做兜底处理
          }
          responseInterceptor({
            data,
            statusCode: uploadRes.statusCode || 200,
          } as any).then(resolve).catch(reject);
        },
        fail: (err) => {
          if (!finalOptions.silent) {
            const msg =
              typeof err?.errMsg === 'string' && err.errMsg.includes('ERR_EMPTY_RESPONSE')
                ? '服务器未返回数据（可能端口/HTTPS/防火墙/网关导致）'
                : '网络连接失败';
            uni.showToast({ title: msg, icon: 'none' });
          }
          reject(err);
        },
      });
    });
  },
};

export default http;