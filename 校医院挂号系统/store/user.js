import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: '',
        patientId: ''
    }),
    actions: {
        setToken(token) {
            this.token = token
        },
        setPatientId(id) {
            this.patientId = id
        },
        logout() {
            this.token = ''
            this.patientId = ''
        }
    }
})


