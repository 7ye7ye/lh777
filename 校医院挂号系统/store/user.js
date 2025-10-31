import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: '',
        userInfo: null,
        isLoggedIn: false
    }),
    actions: {
        setToken(token) {
            this.token = token
            this.isLoggedIn = !!token
            // 同步到本地存储
            if (token) {
                uni.setStorageSync('token', token)
            } else {
                uni.removeStorageSync('token')
            }
        },
        setUserInfo(userInfo) {
            this.userInfo = userInfo
            // 同步到本地存储
            if (userInfo) {
                console.log("store存储的userInfo:",userInfo)
                uni.setStorageSync('userInfo', userInfo)
            } else {
                uni.removeStorageSync('userInfo')
            }
        },
        // 初始化：从本地存储恢复状态
        initFromStorage() {
            const token = uni.getStorageSync('token')
            const userInfo = uni.getStorageSync('userInfo')
            if (token) {
                this.token = token
                this.isLoggedIn = true
            }
            if (userInfo) {
                this.userInfo = userInfo
            }
        },
        logout() {
            this.token = ''
            this.userInfo = null
            this.isLoggedIn = false
            // 清除本地存储
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
        }
    }
})


