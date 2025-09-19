// src/api/userApi.ts
import { http } from '@/utils/request';

// 定义类型（保持 TypeScript 类型安全）
interface LoginData {
  username: string;
  password: string;
}

interface UserInfo {
  id: number;
  username: string;
  name: string;
  role: string;
}

// 统一响应类型（后端返回的外层结构）
interface ApiResponse<T = any> {
    code: number;    // 后端状态码（如 200 成功）
    message: string; // 提示信息
    data: T;         // 实际响应数据（这里 T 指定为 number）
}

// 用户相关 API
export const userApi = {
  // 测试：与后端 @PostMapping("/test") 保持一致
  test: () => http.get<any>('/jeecg-boot/test'),

  // 登录
  login: (data: LoginData) => http.post<UserInfo>('/user/login', data),
  
  // 注册
  register: (data: { username: string; password: string; name: string }) => 
    http.post<{ id: number }>('/user/register', data),
  
  // 获取当前用户信息
  getCurrentUser: () => http.get<UserInfo>('/user/current'),
  
  // 退出登录
  logout: () => http.post<{ success: boolean }>('/user/logout'),
};