import { defineStore } from 'pinia'

export const useGlobalStore = defineStore('global', {
    state: () => ({
        userName: '未登录'
    }),
    actions: {
        setUserName(name) {
            this.userName = name
        }
    }
})


