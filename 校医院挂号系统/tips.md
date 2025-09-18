路由写法：
1.，从 tabBar 页面跳普通页面用 uni.navigateTo
2.加 uni.showToast({ title: '准备跳转', icon: 'none' })。点击按钮后若能弹出提示，说明事件触发正常；若无提示，需要检查按钮是否被遮挡、同层渲染、或外层有pointer-events:none
3.若从普通页面跳转到 tabBar 页面，必须用 uni.switchTab；
4.在小程序端，页面跳转建议使用绝对路径（以 / 开头）来避免相对路径被错误解析。绝对路径 '/pages/login/login'。相对路径会在当前页面目录下解析，pages/profile/profile 去相对跳 pages/login/login 会被拼成 pages/profile/pages/login/login。