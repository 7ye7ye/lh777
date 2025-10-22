// uni-app 全局类型声明
declare global {
  const uni: {
    showModal: (options: {
      title?: string
      content?: string
      editable?: boolean
      placeholderText?: string
      confirmText?: string
      cancelText?: string
      showCancel?: boolean
      success?: (res: { confirm: boolean; content?: string }) => void
    }) => void
    
    showToast: (options: {
      title: string
      icon?: 'success' | 'error' | 'loading' | 'none'
      duration?: number
    }) => void
    
    showLoading: (options: { title: string }) => void
    hideLoading: () => void
    
    navigateTo: (options: { url: string }) => void
    navigateBack: (options?: { delta?: number }) => void
    switchTab: (options: { url: string }) => void
    
    setStorageSync: (key: string, value: any) => void
    getStorageSync: (key: string) => any
    removeStorageSync: (key: string) => void
  }
}

export {}
