<template>
	<view class="container">
		<text>个人中心</text>
		<button type="primary" @click="login">登录</button>
	</view>
</template>

<script setup>
    import { useGlobalStore } from '@/store/global'
    import { storeToRefs } from 'pinia'
	const login = () => {
		console.log('登录')
		uni.showToast({ title: '准备跳转', icon: 'none' })
		uni.navigateTo({
			url: '/pages/login/login'
		})
	}
   
    
</script>

<style>
	.container {
		padding: 24rpx;
	}
</style>


