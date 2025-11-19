import { userApi } from '../../校医院挂号系统/api/user';

// 在页面加载或提交前调用：配置正确的后端地址与前缀
export function configureApiForDoctorBackend() {
  // 医生端后端直连地址
  uni.setStorageSync('BASE_URL', 'http://127.0.0.1:52847');
  // 医生端没有 jeecg-boot 前缀
  uni.setStorageSync('API_PREFIX', '');
}

// 示例：登录提交前确保已配置
export async function handleLogin(userAccount, userPassword) {
  configureApiForDoctorBackend();
  try {
    const res = await userApi.login({ userAccount, userPassword });
    // 后端可能直接返回 DTO 或包在 { code, data }，做兼容
    const payload = res?.data ?? res;
    const token = payload?.token;
    if (token) {
      uni.setStorageSync('token', token);
      uni.showToast({ title: '登录成功', icon: 'success' });
    } else {
      uni.showToast({ title: '登录返回缺少Token', icon: 'none' });
    }
    return payload;
  } catch (e) {
    console.error('登录失败', e);
    uni.showToast({ title: '登录失败', icon: 'none' });
    throw e;
  }
}