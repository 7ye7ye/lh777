<template>
	<view>
		<button type="danger" @click="handleTest">测试</button>
		<button type="primary" @click="handleLogin">登录</button>
		<button type="primary" @click="handleRegister">注册</button>
		<button type="primary" @click="backToHome">返回首页</button>
	</view>
</template>

<script setup>
import { userApi } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const handleTest = () => {
	userApi.test().then(res => {
		console.log(res)
		uni.showToast({
			title: res.data.message,
			icon: 'success'
		})
	})
}

const handleLogin = () => {
	userApi.login({
		username: 'admin',
		password: '123456'
	}).then(res => {
		userStore.setToken(res.data.token)
	})
}
const handleRegister = () => {
	userApi.register({
		username: 'admin',
		password: '123456'
	}).then(res => {
		userStore.setToken(res.data.token)
	})
}
const backToHome = () => {
	uni.switchTab({
		url: '/pages/home/home'
	})
}
</script>
	

<style>

</style>
