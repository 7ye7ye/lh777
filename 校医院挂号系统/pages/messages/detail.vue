<template>
	<view class="container">
		<view class="message-detail-list">
			<view v-for="item in messageDetailList" :key="item.messageId" class="detail-card">
				<view class="card-header">
					<text>{{ formatDateTime(item.createdTime) }}</text>
				</view>
				<view class="card-body">
					<view class="body-title">{{ item.title }}</view>
					<view class="info-row" v-if="item.content.patient_card_no">
						<text class="label">用户就诊卡号：</text>
						<text class="value">{{ item.content.patient_card_no }}</text>
					</view>
					<view class="info-row" v-if="item.content.patient_name">
						<text class="label">用户姓名：</text>
						<text class="value">{{ item.content.patient_name }}</text>
					</view>
					<view class="info-row" v-if="item.content.doctor_name">
						<text class="label">医生姓名：</text>
						<text class="value">{{ item.content.doctor_name }}</text>
					</view>
					<view class="info-row" v-if="item.content.department_name">
						<text class="label">科室名称：</text>
						<text class="value">{{ item.content.department_name }}</text>
					</view>
					<view class="info-row" v-if="item.content.appointment_time">
						<text class="label">预约时间：</text>
						<text class="value">{{ item.content.appointment_time }}</text>
					</view>
					<view class="info-row" v-if="item.content.hospital_remark">
						<text class="label">医院备注：</text>
						<text class="value remark">{{ item.content.hospital_remark }}</text>
					</view>
				</view>
				<view class="card-footer" @click="goToReceipt(item.appointmentId)">
					<text>查看详情</text>
					<image class="arrow-icon" src="/static/icon_arrow_right.png" mode="aspectFit"></image>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				appointmentId: null, // 从上个页面传来的预约ID
				messageDetailList: [] // 存储消息详情列表
			};
		},
		// uni-app生命周期函数，在页面加载时执行，可以获取路由参数
		onLoad(options) {
			if (options.appointmentId) {
				this.appointmentId = options.appointmentId;
				this.fetchMessageDetail();
			} else {
				uni.showToast({ title: '加载失败，缺少预约ID', icon: 'none' });
			}
		},
		methods: {
			fetchMessageDetail() {
				// 【重要】请确保这里的IP地址和端口是正确的
				const apiUrl = `http://10.61.62.249:8095/jeecg-boot/api/messages/detail`;
				
				uni.request({
					url: `${apiUrl}?appointmentId=${this.appointmentId}`,
					method: 'GET',
					success: (res) => {
						if (res.statusCode === 200) {
							// 后端返回的 content 是字符串，需要解析成JSON对象
							this.messageDetailList = res.data.map(item => {
								if (typeof item.content === 'string') {
									try {
										item.content = JSON.parse(item.content);
									} catch (e) {
										item.content = {}; // 解析失败则置为空对象
									}
								}
								return item;
							});
						} else {
							uni.showToast({ title: '加载详情失败', icon: 'none' });
						}
					},
					fail: (err) => {
						console.error('API请求失败:', err);
						uni.showToast({ title: '网络请求失败', icon: 'none' });
					}
				});
			},
			// 跳转到最终的挂号回执单页面
			goToReceipt(appointmentId) {
				// 我们还没有创建回执单页面，先把逻辑写好
				uni.navigateTo({
					url: `/pages/messages/receipt?id=${appointmentId}`
				});
				// uni.showToast({ title: '即将跳转到挂号回执单', icon: 'none' });
			},
			
			// 格式化日期时间
			formatDateTime(dateTimeStr) {
				if (!dateTimeStr) return '';
				return dateTimeStr.replace('T', ' ');
			}
		}
	}
</script>

<style scoped>
	.container {
		background-color: #f5f5f5;
		min-height: 100vh;
		padding: 24rpx;
	}
	.detail-card {
		background-color: #ffffff;
		border-radius: 16rpx;
		margin-bottom: 24rpx;
		overflow: hidden;
	}
	.card-header {
		text-align: center;
		padding: 16rpx;
		font-size: 24rpx;
		color: #999;
		background-color: #fafafa;
	}
	.card-body {
		padding: 32rpx;
	}
	.body-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 24rpx;
	}
	.info-row {
		display: flex;
		font-size: 28rpx;
		line-height: 1.6;
		margin-bottom: 8rpx;
	}
	.label {
		color: #999;
		width: 180rpx; /* 固定标签宽度，让冒号对齐 */
		flex-shrink: 0;
	}
	.value {
		color: #333;
		flex-grow: 1;
	}
	.value.remark {
		color: #ff9900;
	}
	.card-footer {
		border-top: 1rpx solid #f0f0f0;
		padding: 24rpx 32rpx;
		font-size: 28rpx;
		color: #333;
		display: flex;
		justify-content: space-between;
		align-items: center;
		cursor: pointer;
	}
	.arrow-icon {
		width: 28rpx;
		height: 28rpx;
	}
</style>