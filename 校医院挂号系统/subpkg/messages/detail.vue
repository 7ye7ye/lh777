<template>
	<view class="container">
		<view class="message-detail-list" v-if="messageDetail">
			<view class="detail-card" :class="getCardClass(messageDetail.messageType)">
				<view class="card-header">
					<text>{{ formatDateTime(messageDetail.createdTime) }}</text>
				</view>
				<view class="card-body">
					<view class="body-title" :class="getTitleClass(messageDetail.messageType)">
						{{ getDisplayTitle(messageDetail) }}
					</view>
					<view class="info-row" v-if="messageDetail.content.patient_card_no">
						<text class="label">用户就诊卡号：</text>
						<text class="value">{{ messageDetail.content.patient_card_no }}</text>
					</view>
					<view class="info-row" v-if="messageDetail.content.patient_name">
						<text class="label">用户姓名：</text>
						<text class="value">{{ messageDetail.content.patient_name }}</text>
					</view>
					<view class="info-row" v-if="messageDetail.content.doctor_name">
						<text class="label">医生姓名：</text>
						<text class="value">{{ messageDetail.content.doctor_name }}</text>
					</view>
					<view class="info-row" v-if="messageDetail.content.department_name">
						<text class="label">科室名称：</text>
						<text class="value">{{ messageDetail.content.department_name }}</text>
					</view>
					<view class="info-row" v-if="messageDetail.content.appointment_time">
						<text class="label">预约时间：</text>
						<text class="value">{{ messageDetail.content.appointment_time }}</text>
					</view>
					<view class="info-row" v-if="isWaitingMessage(messageDetail) && messageDetail.content.waiting_rank">
						<text class="label">候补序号：</text>
						<text class="value">{{ messageDetail.content.waiting_rank }}</text>
					</view>
					<view class="info-row" v-if="isWaitingMessage(messageDetail) && messageDetail.content.waiting_join_time">
						<text class="label">加入候补时间：</text>
						<text class="value">{{ messageDetail.content.waiting_join_time }}</text>
					</view>
					<view class="info-row" v-if="isWaitingSuccess(messageDetail) && messageDetail.content.promote_time">
						<text class="label">转正时间：</text>
						<text class="value">{{ messageDetail.content.promote_time }}</text>
					</view>
					<view class="info-row" v-if="isCancelMessage(messageDetail) && messageDetail.content.cancel_time">
						<text class="label">退号时间：</text>
						<text class="value">{{ messageDetail.content.cancel_time }}</text>
					</view>
					<view class="info-row" v-if="isCancelMessage(messageDetail) && messageDetail.content.cancel_reason">
						<text class="label">退号原因：</text>
						<text class="value">{{ messageDetail.content.cancel_reason }}</text>
					</view>
					<view class="info-row" v-if="messageDetail.content.hospital_remark">
						<text class="label">医院备注：</text>
						<text class="value remark">{{ messageDetail.content.hospital_remark }}</text>
					</view>
				</view>
				<view class="card-footer" v-if="shouldShowReceipt(messageDetail)" @click="goToReceipt(messageDetail.appointmentId)">
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
				messageId: null, // 从上个页面传来的消息ID
				messageDetail: null // 存储单条消息详情
			};
		},
		// uni-app生命周期函数，在页面加载时执行，可以获取路由参数
		onLoad(options) {
			if (options.messageId) {
				this.messageId = options.messageId;
				this.fetchMessageDetail();
			} else {
				uni.showToast({ title: '加载失败，缺少消息ID', icon: 'none' });
			}
		},
		methods: {
			// 根据消息类型返回卡片样式类
			getCardClass(messageType) {
				switch(messageType) {
					case 'APPOINTMENT_REMINDER':
						return 'card-reminder';
					case 'APPOINTMENT_ONE_HOUR':
						return 'card-onehour';
					case 'APPOINTMENT_WAITING_SUCCESS':
					case 'APPOINTMENT_WAITING_JOIN':
						return 'card-waiting';
					case 'APPOINTMENT_CANCEL':
						return 'card-cancel';
					default:
						return '';
				}
			},

			// 根据消息类型返回标题样式类
			getTitleClass(messageType) {
				switch(messageType) {
					case 'APPOINTMENT_REMINDER':
						return 'title-reminder';
					case 'APPOINTMENT_CANCEL':
						return 'title-cancel';
					case 'APPOINTMENT_ONE_HOUR':
						return 'title-onehour';
					case 'APPOINTMENT_WAITING_SUCCESS':
					case 'APPOINTMENT_WAITING_JOIN':
						return 'title-waiting';
					default:
						return '';
				}
			},

			// 根据消息类型返回显示的标题
			getDisplayTitle(message) {
				if (!message) return '';
				if (message.messageType === 'APPOINTMENT_CANCEL') {
					return '退号成功';
				}
				if (message.messageType === 'APPOINTMENT_ONE_HOUR') {
					return '就诊前一小时提醒';
				}
				if (message.messageType === 'APPOINTMENT_WAITING_SUCCESS') {
					// 检查是否为加号场景
					try {
						const content = typeof message.content === 'string' 
							? JSON.parse(message.content) 
							: message.content;
						if (content && content.source_type === 'add_quota') {
							return '加号成功';
						}
					} catch (e) {
						console.warn('解析消息内容失败', e);
					}
					return '候补挂号成功';
				}
				if (message.messageType === 'APPOINTMENT_WAITING_JOIN') {
					return '已加入候补队列';
				}
				return message.title;
			},

			isWaitingSuccess(message) {
				return message && message.messageType === 'APPOINTMENT_WAITING_SUCCESS';
			},

			isWaitingJoin(message) {
				return message && message.messageType === 'APPOINTMENT_WAITING_JOIN';
			},

			isWaitingMessage(message) {
				return this.isWaitingSuccess(message) || this.isWaitingJoin(message);
			},

			isCancelMessage(message) {
				return message && message.messageType === 'APPOINTMENT_CANCEL';
			},

			shouldShowReceipt(message) {
				if (!message) return false;
				if (this.isWaitingJoin(message)) {
					return false;
				}
				return !!message.appointmentId;
			},

			fetchMessageDetail() {
				// 【重要】调用新的单条消息接口
				const apiUrl = `http://localhost:8095/jeecg-boot/api/messages/${this.messageId}`;
				
				console.log('📤 请求消息详情, messageId =', this.messageId);
				console.log('📤 请求URL:', apiUrl);

				uni.request({
					url: apiUrl,
					method: 'GET',
					header:{
						'X-Access-Token': uni.getStorageSync('token')
					},
					success: (res) => {
						console.log('📥 响应状态码:', res.statusCode);
						console.log('📥 响应数据:', res.data);

						if (res.statusCode === 200 && res.data) {
							// 后端返回的 content 是字符串，需要解析成JSON对象
							let item = res.data;
							if (typeof item.content === 'string') {
								try {
									item.content = JSON.parse(item.content);
								} catch (e) {
									console.error('JSON解析失败:', e);
									item.content = {}; // 解析失败则置为空对象
								}
							}
							this.messageDetail = item;
							console.log('✅ 消息详情加载成功');
						} else {
							console.error('❌ 加载失败，状态码:', res.statusCode);
							uni.showToast({ title: '加载详情失败', icon: 'none' });
						}
					},
					fail: (err) => {
						console.error('❌ API请求失败:', err);
						uni.showToast({ title: '网络请求失败', icon: 'none' });
					}
				});
			},
			// 跳转到最终的挂号回执单页面
			goToReceipt(appointmentId) {
				uni.navigateTo({
					url: `/subpkg/messages/receipt?id=${appointmentId}`

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

	/* 提醒消息标题样式 */
	.body-title.title-reminder {
		color: #ff9900;
	}

	/* 取消消息标题样式 */
	.body-title.title-cancel {
		color: #999;
	}

	/* 一小时前提醒标题样式 */
	.body-title.title-onehour {
		color: #ff4d4f;
	}

	/* 候补成功标题样式 */
	.body-title.title-waiting {
		color: #2f8df6;
	}

	/* 提醒消息卡片样式 */
	.detail-card.card-reminder {
		border-left: 4rpx solid #ff9900;
	}

	/* 一小时前提醒消息卡片样式 */
	.detail-card.card-onehour {
		border-left: 4rpx solid #ff4d4f;
	}

	/* 候补成功卡片样式 */
	.detail-card.card-waiting {
		border-left: 4rpx solid #2f8df6;
	}

	/* 退号卡片样式 */
	.detail-card.card-cancel {
		border-left: 4rpx solid #dcdfe6;
		background-color: #f8f8f8;
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
