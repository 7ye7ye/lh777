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
				// 不要使用 localhost 或 127.0.0.1，而要使用你电脑的局域网IP
				const apiUrl = 'http://10.61.62.249:8095/jeecg-boot/api/messages/list';
				
				// 这里的userId应该是动态获取的，先用测试ID
				const testUserId = 'wuzhizhu_001'; 
				
				uni.request({
					url: `${apiUrl}?userId=${testUserId}`,
					method: 'GET',
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
				// 我们还没有创建详情页，但先把跳转逻辑写好
				uni.navigateTo({
					url: `/pages/messages/detail?appointmentId=${appointmentId}`
				});
			},
			
			// 格式化时间函数
			formatTime(dateTimeStr) {
				if (!dateTimeStr) return '';
				// 简单处理，只取日期部分
				return dateTimeStr.split('T')[0];
			}
		}
	}
</script>
<style scoped>
	.container {
		background-color: #f5f5f5;
		min-height: 100vh;
		padding: 16rpx 0;
	}

	.message-list {
		width: 100%;
	}
	
	.message-card {
		display: flex;
		align-items: center;
		background-color: #ffffff;
		margin: 16rpx 24rpx;
		padding: 24rpx;
		border-radius: 16rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}
	
	.card-icon {
		width: 88rpx;
		height: 88rpx;
		margin-right: 24rpx;
	}
	
	.card-icon image {
		width: 100%;
		height: 100%;
	}
	
	.card-content {
		flex: 1;
		display: flex;
		flex-direction: column;
	}
	
	.card-title-line {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 8rpx;
	}
	
	.card-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}
	
	.card-time {
		font-size: 24rpx;
		color: #999;
	}
	
	.card-summary {
		font-size: 28rpx;
		color: #666;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
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
		margin-bottom: 24rpx;
	}
	
	.empty-text {
		font-size: 28rpx;
		color: #999;
	}
</style>
