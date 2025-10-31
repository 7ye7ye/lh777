"use strict";
const common_vendor = require("../common/vendor.js");
const detectBaseURL = () => {
  const stored = typeof common_vendor.index !== "undefined" ? common_vendor.index.getStorageSync("BASE_URL") : "";
  if (stored)
    return stored;
  const g = typeof globalThis !== "undefined" ? globalThis : typeof window !== "undefined" ? window : {};
  if (g && g.__API_BASE_URL)
    return g.__API_BASE_URL;
  return "http://localhost:8095";
};
const baseURL = detectBaseURL();
const API_PREFIX = (() => {
  try {
    return typeof common_vendor.index !== "undefined" ? common_vendor.index.getStorageSync("API_PREFIX") || "/jeecg-boot" : "/jeecg-boot";
  } catch (_) {
    return "/jeecg-boot";
  }
})();
const requestInterceptor = (options) => {
  if (options.url && !/^https?:\/\//i.test(options.url)) {
    let path = options.url;
    if (!path.startsWith("/"))
      path = `/${path}`;
    const usePrefix = API_PREFIX && !(options == null ? void 0 : options.noPrefix);
    if (usePrefix) {
      if (!path.startsWith(API_PREFIX + "/")) {
        path = API_PREFIX + path;
      }
    }
    options.url = baseURL + path;
  }
  const method = (options.method || "GET").toUpperCase();
  if (options.params && (method === "GET" || method === "DELETE")) {
    const toUrlEncoded = (obj = {}) => Object.keys(obj).map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(obj[k] ?? "")}`).join("&");
    const query = toUrlEncoded(options.params);
    if (query) {
      options.url += (options.url.includes("?") ? "&" : "?") + query;
    }
    delete options.params;
  }
  const header = { ...options.header };
  const contentType = options.contentType || "json";
  if (contentType === "json") {
    header["Content-Type"] = "application/json";
  } else if (contentType === "form") {
    header["Content-Type"] = "application/x-www-form-urlencoded";
    if (options.data && typeof options.data === "object") {
      const toUrlEncoded = (obj = {}) => Object.keys(obj).map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(obj[k] ?? "")}`).join("&");
      options.data = toUrlEncoded(options.data);
    }
  }
  const skipAuth = !!options.skipAuth;
  const token = common_vendor.index.getStorageSync("token");
  if (!skipAuth && token) {
    header["Authorization"] = `Bearer ${token}`;
    header["X-Access-Token"] = token;
  }
  options.header = header;
  return options;
};
const responseInterceptor = (response) => {
  const { data, statusCode } = response;
  common_vendor.index.__f__("log", "at utils/request.ts:85", "响应拦截器 - 原始响应:", response);
  common_vendor.index.__f__("log", "at utils/request.ts:86", "响应拦截器 - data:", data);
  if (statusCode < 200 || statusCode >= 300) {
    const httpMsg = statusCode === 502 ? "网关错误(502)：后端服务不可达或路由未配置" : statusCode === 404 ? "接口不存在(404)" : statusCode >= 500 ? "服务器错误" : `请求失败: ${statusCode}`;
    common_vendor.index.showToast({ title: httpMsg, icon: "none" });
    return Promise.reject(new Error(httpMsg));
  }
  if (data && typeof data === "object" && ("code" in data || "success" in data)) {
    const code = data.code;
    const success = data.success;
    const ok = success === true || code === 200 || code === 0;
    if (!ok) {
      const errorMsg = data.description || data.message || "操作失败";
      common_vendor.index.showToast({ title: errorMsg, icon: "none" });
      return Promise.reject(new Error(errorMsg));
    }
    const payload = data.result !== void 0 ? data.result : data.data !== void 0 ? data.data : data;
    return Promise.resolve(payload);
  }
  if (data && typeof data === "object" && data.body !== void 0) {
    common_vendor.index.__f__("log", "at utils/request.ts:126", "检测到ResponseEntity格式，提取body:", data.body);
    return Promise.resolve(data.body);
  }
  common_vendor.index.__f__("log", "at utils/request.ts:131", "返回原始data:", data);
  return Promise.resolve(data);
};
const request = (options) => {
  const finalOptions = requestInterceptor(options);
  return new Promise((resolve, reject) => {
    common_vendor.index.request({
      ...finalOptions,
      timeout: finalOptions.timeout ?? 8e3,
      // 发起请求
      success: (res) => {
        responseInterceptor(res).then(resolve).catch(reject);
      },
      // 处理网络错误（如断网/空响应）
      fail: (err) => {
        if (!finalOptions.silent) {
          const msg = typeof (err == null ? void 0 : err.errMsg) === "string" && err.errMsg.includes("ERR_EMPTY_RESPONSE") ? "服务器未返回数据（可能端口/HTTPS/防火墙/网关导致）" : "网络连接失败";
          common_vendor.index.showToast({ title: msg, icon: "none" });
        }
        reject(err);
      }
    });
  });
};
const http = {
  get: (url, params, options) => request({ ...options, url, method: "GET", params }),
  post: (url, data, options) => request({ contentType: "json", ...options, url, method: "POST", data }),
  put: (url, data, options) => request({ contentType: "json", ...options, url, method: "PUT", data }),
  delete: (url, params, options) => request({ ...options, url, method: "DELETE", params }),
  postForm: (url, data, options) => request({ contentType: "form", ...options, url, method: "POST", data })
};
exports.http = http;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/request.js.map
