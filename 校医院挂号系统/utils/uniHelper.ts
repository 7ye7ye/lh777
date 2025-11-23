/**
 * uni-app 工具函数封装
 */

// uni.showModal 的封装
export function uniShowModal(options: {
  title?: string
  content?: string
  editable?: boolean
  placeholderText?: string
  confirmText?: string
  cancelText?: string
  showCancel?: boolean
  success?: (res: { confirm: boolean; content?: string }) => void
}): void {
  uni.showModal(options)
}

// uni.showToast 的封装
export function uniShowToast(options: {
  title: string
  icon?: 'success' | 'error' | 'loading' | 'none'
  duration?: number
}): void {
  uni.showToast(options)
}

// uni.showLoading 的封装
export function uniShowLoading(options: { title: string }): void {
  uni.showLoading(options)
}

// uni.hideLoading 的封装
export function uniHideLoading(): void {
  uni.hideLoading()
}

// uni.navigateTo 的封装
export function uniNavigateTo(options: { url: string } | string): void {
  const opt = typeof options === 'string' ? { url: options } : options
  if (!opt || typeof opt.url !== 'string' || !opt.url) {
    throw new Error('navigateTo: url is required')
  }
  uni.navigateTo(opt)
}

// uni.navigateBack 的封装
export function uniNavigateBack(options?: { delta?: number }): void {
  uni.navigateBack(options)
}

// uni.switchTab 的封装
export function uniSwitchTab(options: { url: string } | string): void {
  const opt = typeof options === 'string' ? { url: options } : options
  if (!opt || typeof opt.url !== 'string' || !opt.url) {
    throw new Error('switchTab: url is required')
  }
  uni.switchTab(opt)
}