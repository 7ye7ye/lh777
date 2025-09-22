// 封装uni-app导航方法
export const uniNavigateTo = (options) => {
  return new Promise((resolve, reject) => {
    uni.navigateTo({
      ...options,
      success: resolve,
      fail: reject
    })
  })
}

// 封装uni-app提示框方法
export const uniShowToast = (options) => {
  return new Promise((resolve) => {
    uni.showToast({
      icon: 'none',
      duration: 2000,
      ...options,
      success: resolve
    })
  })
}

// 可以根据需要添加其他常用方法
export const uniSwitchTab = (options) => {
  return new Promise((resolve, reject) => {
    uni.switchTab({
      ...options,
      success: resolve,
      fail: reject
    })
  })
}