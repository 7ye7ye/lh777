<template>
	<view class="container">
		<view v-if="groupedMessages.length > 0" class="message-list">
			<view v-for="group in groupedMessages" :key="group.appointmentId" class="message-card" @click="goToDetail(group.appointmentId)">
				<view class="card-icon">
					<image src="/static/info_message.png" mode="aspectFit"></image>
				</view>
				<view class="card-content">
					<view class="card-title-line">
						<text class="card-title">预约挂号</text>
						<text class="card-time">{{ formatTime(group.latestMessage.createdTime) }}</text>
					</view>
					<view class="card-summary">{{ group.latestMessage.title }}</view>
				</view>
			</view>
		</view>
		
		<view v-else class="empty-container">
			<image class="empty-icon" src="/static/empty_message.png" mode="aspectFit"></image>
			<text class="empty-text">暂无任何消息</text>
		</view>

		<!-- 医生端入口按钮 -->
		<button class="doctor-entry-btn" @click="goDoctorMain">进入医生端</button>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				messageList: [], // 存储从后端获取的原始消息列表
				loading: false // 加载状态，防止重复请求
			};
		},
		computed: {
			// 计算属性，将原始列表处理成按 appointmentId 分组的结构
			groupedMessages() {
				if (this.messageList.length === 0) {
					return [];
				}
				
				const groups = {};
				this.messageList.forEach(msg => {
					// 如果分组不存在，则创建
					if (!groups[msg.appointmentId]) {
						groups[msg.appointmentId] = {
							appointmentId: msg.appointmentId,
							latestMessage: msg // 默认第一条就是最新的
						};
					}
					// 因为列表已经是降序，所以第一条就是最新的，无需再比较时间
				});
				
				// 将分组对象转换为数组并返回
				return Object.values(groups);
			}
		},
		// uni-app生命周期函数，每次进入页面都会触发
		onShow() {
			this.fetchMessageList();
		},
		// uni-app生命周期函数，监听下拉刷新
		onPullDownRefresh() {
			this.fetchMessageList();
		},
		methods: {
			// 从后端接口获取消息列表
			fetchMessageList() {
				if (this.loading) return;
				this.loading = true;
				
				// 这里的IP地址和端口需要换成你后端项目运行的实际地址
				// 不要使用 localhost 或 127.0.0.1，而要使用你电脑的局域网IP 校园网：10.61.62.249
				const apiUrl = 'http://10.61.62.249:8095/jeecg-boot/api/messages/list';
				
				// 这里的userId应该是动态获取的，先用测试ID
				const testUserId = 'wuzhizhu_001'; 
				
				uni.request({
					url: `${apiUrl}?userId=${testUserId}`,
					method: 'GET',
					header: {
							// 'X-Access-Token' 是 jeecg-boot 框架默认的 Token 键名
							// 'token' 是您调用 uni.setStorageSync 存入时的键名，请确保一致
							'X-Access-Token': uni.getStorageSync('token') 
						},
					success: (res) => {
						if (res.statusCode === 200) {
							this.messageList = res.data;
						} else {
							uni.showToast({ title: '加载失败', icon: 'none' });
						}
					},
					fail: (err) => {
						console.error('API请求失败:', err);
						uni.showToast({ title: '网络请求失败', icon: 'none' });
					},
					complete: () => {
						this.loading = false;
						uni.stopPullDownRefresh(); // 停止下拉刷新的动画
					}
				});
			},
			
			// 跳转到消息详情列表页
			goToDetail(appointmentId) {
				uni.navigateTo({
					url: `/subpkg/messages/detail?appointmentId=${appointmentId}`
				});
			},
			
			// 格式化时间函数
			formatTime(dateTimeStr) {
				if (!dateTimeStr) return '';
				// 简单处理，只取日期部分
				return dateTimeStr.split('T')[0];
			},

			// 跳转到医生端
			goDoctorMain() {
				uni.navigateTo({ 
					url: '/pages/doctor/schedule/main' 
				});
			}
		}
	}
</script>

<style scoped>
	.container {
		background-color: #f5f5f5;
		min-height: 100vh;
		padding: 16rpx 0;
		position: relative;
	}

	.message-list {
		padding: 0 16rpx;
	}

	.message-card {
		background-color: #fff;
		border-radius: 12rpx;
		padding: 20rpx;
		margin-bottom: 16rpx;
		display: flex;
		align-items: flex-start;
	}

	.card-icon {
		width: 80rpx;
		height: 80rpx;
		margin-right: 16rpx;
	}

	.card-icon image {
		width: 100%;
		height: 100%;
	}

	.card-content {
		flex: 1;
	}

	.card-title-line {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 8rpx;
	}

	.card-title {
		font-size: 32rpx;
		color: #333;
		font-weight: bold;
	}

	.card-time {
		font-size: 24rpx;
		color: #999;
	}

	.card-summary {
		font-size: 28rpx;
		color: #666;
	}

	.empty-container {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding-top: 200rpx;
	}

	.empty-icon {
		width: 200rpx;
		height: 200rpx;
		margin-bottom: 20rpx;
	}

	.empty-text {
		font-size: 28rpx;
		color: #999;
	}

	/* 医生端入口按钮样式 */
	.doctor-entry-btn {
		position: fixed;
		bottom: 40rpx;
		left: 50%;
		transform: translateX(-50%);
		background: #479fff;
		color: #fff;
		border-radius: 999rpx;
		padding: 20rpx 40rpx;
		font-size: 28rpx;
		box-shadow: 0 4rpx 16rpx rgba(71,159,255,0.3);
	}
</style>