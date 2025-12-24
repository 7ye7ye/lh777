/**
 * 权限控制工具
 * 统一管理需要登录才能访问的功能
 */

import { useUserStore } from '../store/user'
import { uniShowModal, uniNavigateTo, uniSwitchTab } from '../utils/uniHelper'

// 功能标识类型
type FeatureKey = 
  | 'mycard' 
  | 'register' 
  | 'payment' 
  | 'report'
  | 'my_card' 
  | 'my_patient' 
  | 'my_doctor' 
  | 'records' 
  | 'settings'
  | 'message_detail' 
  | 'hospital_dept'
  | 'disease_booking'
  | 'department_booking'
  | 'referral_records'
  | 'department_introduction'
  | 'doctor_introduction'

// 需要登录才能访问的功能配置
export const AUTH_REQUIRED_FEATURES = {
  // 首页功能
  HOME: {
    VISIT_CARD: 'visit_card' as const, // 电子就诊卡
    REGISTER: 'register' as const, // 挂号相关
    PAYMENT: 'payment' as const, // 缴费
    REPORT: 'report' as const, // 报告查询
    DISEASE_BOOKING: 'disease_booking' as const, // 按疾病挂号
    DEPARTMENT_BOOKING: 'department_booking' as const, // 按科室挂号
    REFERRAL_RECORDS: 'referral_records' as const, // 转诊记录
    DEPARTMENT_INTRODUCTION: 'department_introduction' as const, // 科室介绍
    DOCTOR_INTRODUCTION: 'doctor_introduction' as const, // 专家介绍
  },
  
  // 个人中心功能
  PROFILE: {
    MY_CARD: 'my_card' as const, // 我的就诊卡
    MY_PATIENT: 'my_patient' as const, // 我的就诊人
    MY_DOCTOR: 'my_doctor' as const, // 我的医生
    RECORDS: 'records' as const, // 就诊记录
    SETTINGS: 'settings' as const, // 设置
  },
  
  // 其他功能
  OTHER: {
    MESSAGE_DETAIL: 'message_detail' as const, // 消息详情
    HOSPITAL_DEPT: 'hospital_dept' as const, // 科室详情
  }
} as const

// 功能对应的提示文案
export const AUTH_MESSAGES: Record<string, string> = {
  [AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD]: '请先登录后查看电子就诊卡',
  [AUTH_REQUIRED_FEATURES.HOME.REGISTER]: '请先登录后进行挂号',
  [AUTH_REQUIRED_FEATURES.HOME.PAYMENT]: '请先登录后进行缴费',
  [AUTH_REQUIRED_FEATURES.HOME.REPORT]: '请先登录后查询报告',
  [AUTH_REQUIRED_FEATURES.HOME.DISEASE_BOOKING]: '请先登录后使用按疾病挂号',
  [AUTH_REQUIRED_FEATURES.HOME.DEPARTMENT_BOOKING]: '请先登录后使用按科室挂号',
  [AUTH_REQUIRED_FEATURES.HOME.REFERRAL_RECORDS]: '请先登录后查看转诊记录',
  [AUTH_REQUIRED_FEATURES.HOME.DEPARTMENT_INTRODUCTION]: '请先登录后查看科室介绍',
  [AUTH_REQUIRED_FEATURES.HOME.DOCTOR_INTRODUCTION]: '请先登录后查看专家介绍',
  
  [AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD]: '请先登录后查看我的就诊卡',
  [AUTH_REQUIRED_FEATURES.PROFILE.MY_PATIENT]: '请先登录后管理就诊人',
  [AUTH_REQUIRED_FEATURES.PROFILE.MY_DOCTOR]: '请先登录后查看我的医生',
  [AUTH_REQUIRED_FEATURES.PROFILE.RECORDS]: '请先登录后查看就诊记录',
  [AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS]: '请先登录后进行设置',
  
  [AUTH_REQUIRED_FEATURES.OTHER.MESSAGE_DETAIL]: '请先登录后查看消息详情',
  [AUTH_REQUIRED_FEATURES.OTHER.HOSPITAL_DEPT]: '请先登录后查看科室详情',
}

// 功能对应的跳转路径
export const AUTH_REDIRECTS: Record<string, string> = {
  [AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD]: '/subpkg/profile/personal/mycard',
  [AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD]: '/subpkg/profile/personal/mycard',
  [AUTH_REQUIRED_FEATURES.PROFILE.MY_PATIENT]: '/subpkg/profile/personal/mypatient',
  [AUTH_REQUIRED_FEATURES.PROFILE.MY_DOCTOR]: '/subpkg/profile/personal/mydoctor',
  [AUTH_REQUIRED_FEATURES.PROFILE.RECORDS]: '/subpkg/profile/records/register-record',
  [AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS]: '/subpkg/profile/settings/privacy',
}

/**
 * 检查是否需要登录
 * @param feature - 功能标识
 * @returns 是否需要登录
 */
export function requiresAuth(feature: FeatureKey): boolean {
  return Object.values(AUTH_REQUIRED_FEATURES).some(category => 
    Object.values(category).includes(feature as any)
  )
}

/**
 * 获取功能的提示文案
 * @param feature - 功能标识
 * @returns 提示文案
 */
export function getAuthMessage(feature: FeatureKey): string {
  return AUTH_MESSAGES[feature] || '该功能需要登录后使用'
}

/**
 * 获取功能的跳转路径
 * @param feature - 功能标识
 * @returns 跳转路径，如果没有配置则返回null
 */
export function getAuthRedirect(feature: FeatureKey): string | null {
  return AUTH_REDIRECTS[feature] || null
}

/**
 * 检查用户是否已登录
 * @returns 是否已登录
 */
export function isLoggedIn(): boolean {
  const userStore = useUserStore()
  return !!userStore.isLoggedIn
}

/**
 * 权限检查并处理
 * @param feature - 功能标识
 * @param options - 选项
 * @param options.onSuccess - 已登录时的回调
 * @param options.onFail - 未登录时的回调
 * @param options.showModal - 是否显示弹窗提示（默认true）
 * @param options.customMessage - 自定义提示文案
 * @returns 是否通过权限检查
 */
export function checkAuth(
  feature: FeatureKey, 
  options: {
    onSuccess?: () => void
    onFail?: () => void
    showModal?: boolean
    customMessage?: string
  } = {}
): boolean {
  const { onSuccess, onFail, showModal = true, customMessage } = options
  
  if (!isLoggedIn()) {
    if (showModal) {
      const message = customMessage || getAuthMessage(feature)
      uniShowModal({
        title: '温馨提示',
        content: message,
        confirmText: '去登录',
        success: (res: { confirm: boolean }) => {
          if (res.confirm) {
            uniNavigateTo({ url: '/subpkg/auth/login' })
          }
          onFail && onFail()
        }
      })
    } else {
      onFail && onFail()
    }
    return false
  }
  
  onSuccess && onSuccess()
  return true
}

/**
 * 带权限检查的页面跳转
 * @param feature - 功能标识
 * @param url - 跳转路径
 * @param options - 选项
 * @param options.customMessage - 自定义提示文案
 */
export function navigateWithAuth(
  feature: FeatureKey, 
  url: string, 
  options: { customMessage?: string } = {}
): void {
  checkAuth(feature, {
    onSuccess: () => {
      uniNavigateTo({ url })
    },
    customMessage: options.customMessage
  })
}

/**
 * 带权限检查的tabBar跳转
 * @param feature - 功能标识
 * @param url - 跳转路径
 * @param options - 选项
 * @param options.customMessage - 自定义提示文案
 */
export function switchTabWithAuth(
  feature: FeatureKey, 
  url: string, 
  options: { customMessage?: string } = {}
): void {
  checkAuth(feature, {
    onSuccess: () => {
      uniSwitchTab({ url })
    },
    customMessage: options.customMessage
  })
}

/**
 * 创建权限检查的点击处理函数
 * @param feature - 功能标识
 * @param target - 跳转路径或处理函数
 * @param options - 选项
 * @returns 点击处理函数
 */
export function createAuthHandler(
  feature: FeatureKey, 
  target: string | (() => void), 
  options: { customMessage?: string } = {}
): () => void {
  return () => {
    checkAuth(feature, {
      onSuccess: () => {
        if (typeof target === 'function') {
          target()
        } else {
          console.log('Attempting to navigate to:', target)
          uniNavigateTo({
            url: target
          })
        }
      },
      customMessage: options.customMessage
    })
  }
}