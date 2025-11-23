<template>
	<view class="page-bg">
		<view class="list-header">挂号记录</view>
		<view class="record-list">
			<view class="record-item" v-for="item in records" :key="item.id" @click="cancelRecord(item)">
				<view class="row">
					<text class="label">科室：</text>
					<text>{{ item.dept }}</text>
				</view>
				<view class="row">
					<text class="label">医生：</text>
					<text>{{ item.doctor }}</text>
				</view>
				<view class="row">
					<text class="label">时间：</text>
					<text>{{ item.time }}</text>
				</view>
			</view>
			<view v-if="records.length === 0" class="empty-text">暂无挂号记录</view>
		</view>
		<button class="main-btn" @click="getRegisterRecords">刷新</button>
	</view>
</template>

<script setup>
	import {
		ref,
		onMounted
	} from 'vue'
	import {
		getRegistrationRecords,
		cancelRegistration
	} from '@/api/registration'

	const records = ref([])
	const doctorMap = {
		1: '张医生',
		14: '孙医生',
		15: '周医生',
		16: '钱医生',
		11: '陈医生'
	}

	const scheduleMap = {
		1: '内科',
		4: '神经内科',
		5: '神经内科',
		6: '神经内科',
		10: '心内科'
	}


	// 时间格式化（只取日期和时段）
	const slotText = (slot) => {
		switch (slot) {
			case 'morning':
				return '上午'
			case 'afternoon':
				return '下午'
			case 'evening':
				return '晚上'
			default:
				return slot || ''
		}
	}

	// 获取挂号记录
	const getRegisterRecords = async () => {
		const patientId = 1;

		try {
			const res = await getRegistrationRecords(patientId);

			console.log('挂号记录 res:', res); // ✅ 直接打印整个返回数组

			const list = Array.isArray(res) ? res.filter(item => item.status === 1) : [];

			records.value = list.map(item => ({
				id: item.recordId,
				dept: scheduleMap[item.scheduleId] || `科室ID: ${item.scheduleId}`,
				doctor: doctorMap[item.doctorId] || `医生ID: ${item.doctorId}`,
				time: item.registerTime
			}))

			uni.showToast({
				title: '获取成功',
				icon: 'success'
			});
		} catch (e) {
			console.error('获取挂号记录失败:', e);
			uni.showToast({
				title: '获取失败',
				icon: 'none'
			});
		}
	}

	// 页面加载时自动获取
	onMounted(() => {
		getRegisterRecords()
	})
	
	const cancelRecord = (item) => {
	  uni.showModal({
	    title: '取消挂号',
	    content: `是否取消【${item.dept} - ${item.doctor}】的挂号？`,
	    success: async (res) => {
	      if (res.confirm) {
	        try {
	          const result = await cancelRegistration(item.id)
	
	          console.log("取消挂号返回：", result)
	
	          uni.showToast({
	            title: '取消成功',
	            icon: 'success'
	          })
	
	          // 重新刷新挂号记录
	          getRegisterRecords()
	
	        } catch (err) {
	          console.error("取消挂号失败：", err)
	          uni.showToast({
	            title: '取消失败',
	            icon: 'none'
	          })
	        }
	      }
	    }
	  })
	}

</script>

<style scoped>
	.page-bg {
		min-height: 100vh;
		background: #f8faff;
	}

	.list-header {
		font-size: 36rpx;
		font-weight: bold;
		padding: 32rpx;
	}

	.record-list {
		background: #fff;
		margin: 24rpx;
		border-radius: 16rpx;
		padding: 32rpx;
	}

	.record-item {
		border-bottom: 1px solid #eee;
		padding: 16rpx 0;
	}

	.row {
		display: flex;
		align-items: center;
		margin-bottom: 8rpx;
	}

	.label {
		color: #888;
		width: 100rpx;
	}

	.main-btn {
		width: 100%;
		margin-top: 32rpx;
		background: #3a9cff;
		color: #fff;
		padding: 20rpx;
		border-radius: 12rpx;
	}

	.empty-text {
		text-align: center;
		color: #999;
		padding: 32rpx 0;
		font-size: 28rpx;
	}
</style>