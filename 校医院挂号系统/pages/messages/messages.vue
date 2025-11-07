<template>
	<view class="container">
		<view v-if="groupedMessages.length > 0" class="message-list">
			<view v-for="message in groupedMessages" :key="message.messageId" class="message-card" @click="goToDetail(message.messageId)">
				<view class="card-icon">
					<image :src="getMessageIcon(message.messageType)" mode="aspectFit"></image>
				</view>
				<view class="card-content">
					<view class="card-title-line">
						<text class="card-title" :class="getMessageTitleClass(message.messageType)">
							{{ getMessageCategory(message.messageType) }}
						</text>
						<text class="card-time">{{ formatTime(message.createdTime) }}</text>
					</view>
					<view class="card-summary">{{ message.title }}</view>
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
		// 计算属性，直接返回消息列表（不再分组，每条消息独立显示）
		groupedMessages() {
			if (this.messageList.length === 0) {
				return [];
			}
			// 直接返回消息列表，每条消息显示一个独立的卡片
			return this.messageList;
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
			// 获取当前登录用户的ID
			getCurrentUserId() {
				// 方式1: 从本地存储中获取userInfo
				const userInfo = uni.getStorageSync('userInfo');
				console.log('=== getUserId 调试 ===');
				console.log('userInfo:', userInfo);
				
				if (userInfo && userInfo.userId) {
					console.log('✅ 方式1成功: userInfo.userId =', userInfo.userId);
					return userInfo.userId;
				}
				
				// 方式2: 如果userInfo结构不同，尝试其他字段
				// 根据后端返回的实际字段调整，可能是 id、user_id、userId 等
				if (userInfo && userInfo.id) {
					console.log('✅ 方式2成功: userInfo.id =', userInfo.id);
					return userInfo.id;
				}
				if (userInfo && userInfo.user_id) {
					console.log('✅ 方式2成功: userInfo.user_id =', userInfo.user_id);
					return userInfo.user_id;
				}
				
				// 方式3: 开发测试阶段，如果未登录，使用测试ID（生产环境应删除）
				console.warn('⚠️ 方式3: 未找到登录用户信息，使用测试ID');
				return '262'; // 对应你的账号 24301018（hos_user表中的user_id=262）
			},
			
			// 根据消息类型返回对应的图标
			getMessageIcon(messageType) {
				switch(messageType) {
					case 'APPOINTMENT_SUCCESS':
						return '/static/info_message.png';
					case 'APPOINTMENT_CANCEL':
						return '/static/info_message.png';
					case 'APPOINTMENT_REMINDER':
						return '/static/info_message.png'; // 可以替换为专门的提醒图标
					default:
						return '/static/info_message.png';
				}
			},
			
			// 根据消息类型返回分类标签
			getMessageCategory(messageType) {
				switch(messageType) {
					case 'APPOINTMENT_SUCCESS':
						return '预约挂号';
					case 'APPOINTMENT_CANCEL':
						return '预约挂号';
					case 'APPOINTMENT_REMINDER':
						return '就诊提醒';
					default:
						return '系统消息';
				}
			},
			
			// 根据消息类型返回标题样式类
			getMessageTitleClass(messageType) {
				switch(messageType) {
					case 'APPOINTMENT_REMINDER':
						return 'title-reminder';
					case 'APPOINTMENT_CANCEL':
						return 'title-cancel';
					default:
						return '';
				}
			},
			
			// 从后端接口获取消息列表
			fetchMessageList() {
				if (this.loading) return;
				this.loading = true;
				
				// 获取当前登录用户的ID
				let userId = this.getCurrentUserId();
				console.log('📤 准备请求消息列表, userId =', userId);
				
				if (!userId) {
					uni.showToast({ title: '请先登录', icon: 'none' });
					this.loading = false;
					return;
				}
				
				// 这里的IP地址和端口需要换成你后端项目运行的实际地址
				// 不要使用 localhost 或 127.0.0.1，而要使用你电脑的局域网IP 校园网：10.61.62.249
				const apiUrl = 'http://10.61.62.249:8095/jeecg-boot/api/messages/list';
				const requestUrl = `${apiUrl}?userId=${userId}`;
				console.log('📤 请求URL:', requestUrl);
				
				uni.request({
					url: requestUrl,
					method: 'GET',
					header: {
							// 'X-Access-Token' 是 jeecg-boot 框架默认的 Token 键名
							// 'token' 是您调用 uni.setStorageSync 存入时的键名，请确保一致
							'X-Access-Token': uni.getStorageSync('token') 
						},
					success: (res) => {
						console.log('📥 API响应状态码:', res.statusCode);
						console.log('📥 API响应数据:', res.data);
						
						if (res.statusCode === 200) {
							this.messageList = res.data;
							console.log('📋 消息列表长度:', this.messageList ? this.messageList.length : 0);
						} else {
							console.error('❌ 加载失败，状态码:', res.statusCode);
							uni.showToast({ title: '加载失败', icon: 'none' });
						}
					},
					fail: (err) => {
						console.error('❌ API请求失败:', err);
						uni.showToast({ title: '网络请求失败', icon: 'none' });
					},
					complete: () => {
						this.loading = false;
						uni.stopPullDownRefresh(); // 停止下拉刷新的动画
					}
				});
			},
			
			// 跳转到消息详情页（传递单条消息ID）
			goToDetail(messageId) {
				uni.navigateTo({
					url: `/subpkg/messages/detail?messageId=${messageId}`
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
	
	/* 提醒消息标题样式 */
	.card-title.title-reminder {
		color: #ff9900;
	}
	
	/* 取消消息标题样式 */
	.card-title.title-cancel {
		color: #999;
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