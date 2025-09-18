<template>
	<view class="content">
		<image class="logo" src="/static/logo.png"></image>
		<view class="text-area">
			<text class="title">{{title}}</text>
		</view>
		<view class="text-area">
			<text>当前用户：{{ userName }}</text>
		</view>
		<button @click="switchUser">切换用户</button>
	</view>
</template>

<script setup>
    import { useGlobalStore } from '@/store/global'
    import { storeToRefs } from 'pinia'
    const title='首页4'
	const globalStore = useGlobalStore()
	const { userName } = storeToRefs(globalStore)
	const switchUser = () => {
		globalStore.setUserName(userName.value === '未登录' ? '测试用户' : '未登录')
	}
    
</script>

<style>
	.content {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}

	.logo {
		height: 200rpx;
		width: 200rpx;
		margin-top: 200rpx;
		margin-left: auto;
		margin-right: auto;
		margin-bottom: 50rpx;
	}

	.text-area {
		display: flex;
		justify-content: center;
	}

	.title {
		font-size: 36rpx;
		color: #8f8f94;
	}
</style>
