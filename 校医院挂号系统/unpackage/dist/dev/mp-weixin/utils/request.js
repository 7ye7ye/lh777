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
const API_PREFIX = "/jeecg-boot";
const requestInterceptor = (options) => {
  if (options.url && !/^https?:\/\//i.test(options.url)) {
    let path = options.url;
    if (!path.startsWith("/"))
      path = `/${path}`;
    if (!path.startsWith(API_PREFIX + "/")) {
      path = API_PREFIX + path;
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
  }
  options.header = header;
  return options;
};
const responseInterceptor = (response) => {
  const { data, statusCode } = response;
  if (statusCode < 200 || statusCode >= 300) {
    common_vendor.index.showToast({ title: `请求失败: ${statusCode}`, icon: "none" });
    return Promise.reject(new Error(`HTTP Error: ${statusCode}`));
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
    return Promise.resolve(data.data !== void 0 ? data.data : data);
  }
  return Promise.resolve(data);
};
const request = (options) => {
  const finalOptions = requestInterceptor(options);
  return new Promise((resolve, reject) => {
    common_vendor.index.request({
      ...finalOptions,
      // 发起请求
      success: (res) => {
        responseInterceptor(res).then(resolve).catch(reject);
      },
      // 处理网络错误（如断网）
      fail: (err) => {
        if (!finalOptions.silent) {
          common_vendor.index.showToast({ title: "网络连接失败", icon: "none" });
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
