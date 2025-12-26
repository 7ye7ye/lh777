<template>
	<view class="page-bg">
		<view class="page-header">
			<text class="title">挂号支付</text>
		</view>

		<!-- 挂号信息 -->
		<view class="section card">
			<text class="label">挂号信息</text>
			<view class="info-item"><text>科室：</text>{{ dept }}</view>
			<view class="info-item"><text>医生：</text>{{ doctor }}</view>
			<view class="info-item"><text>时间：</text>{{ time }}</view>
			<view class="info-item"><text>挂号费：</text><text class="price">¥{{ fee }}</text></view>
		</view>
		
		<!-- 患者类型（根据账号类型自动锁定） -->
		<view class="section card">
		  <text class="label">就诊人身份</text>
		  <view class="patient-type-list">
		    <view 
		      v-for="type in patientTypeList" 
		      :key="type.value"
		      class="patient-type-item"
		      :class="{ 
		        selected: patientType === type.value,
		        disabled: isTypeLocked && patientType !== type.value
		      }"
		      @click="!isTypeLocked && selectPatientType(type.value)"
		    >
		      <text class="type-icon">{{ getTypeIcon(type.value) }}</text>
		      <text class="patient-text">{{ type.label }}</text>
		      <text v-if="patientType === type.value" class="check-icon">✓</text>
		      <text v-if="isTypeLocked && patientType === type.value" class="lock-icon">🔒</text>
		    </view>
		  </view>
		  <view v-if="isTypeLocked" class="type-lock-tip">
		    <text>身份已根据账号类型自动锁定，不可更改</text>
		  </view>
		</view>


		<!-- 支付方式 -->
		<view class="section card">
			<text class="label">选择支付方式</text>
			<view v-for="method in paymentMethods" :key="method.value" class="pay-item"
				:class="{ selected: selectedMethod === method.value }" @click="selectedMethod = method.value">
				<view class="pay-left">
					<image v-if="method.icon" :src="method.icon" class="icon"></image>
					<text>{{ method.name }}</text>
				</view>
				<view v-if="selectedMethod === method.value" class="check">✔️</view>
			</view>
		</view>

		<!-- 支付按钮 -->
		<view class="bottom-btn">
			<button class="pay-btn" :disabled="!selectedMethod" @click="onPay">
				立即支付
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { createRegistration, getRegistrationRecords } from '../../api/registration' // 挂号接口
import { ensurePatientCard } from '@/utils/patientHelper'

// ------------------ 挂号信息 ------------------
const dept = ref('')
const doctor = ref('')
const time = ref('')
const originalFee = 50 // 挂号原价（可按需要调整或从后端获取）
const fee = ref(0) // 实际支付金额
const patientType = ref(0) // 患者类型
const isTypeLocked = ref(false) // 是否锁定身份类型
const doctorId = ref(null)
const typeId = ref(null)
const scheduleId = ref(null)
const deptId = ref(0)
const currentPatient = ref(null)

// 患者类型显示列表
const patientTypeList = ref([
  { value: 1, label: '学生', icon: '' },
  { value: 2, label: '教师', icon: '' },
  { value: 3, label: '职工', icon: '' }
])

// ------------------ 支付方式 ------------------
const paymentMethods = ref([{
		name: '微信支付',
		value: 'wechat',
		icon: ''
	},
	{
		name: '支付宝支付',
		value: 'alipay',
		icon: ''
	},
	{
		name: '医保卡支付',
		value: 'medical',
		icon: ''
	}
])
const selectedMethod = ref(null)


// ------------------ 页面加载 ------------------
const loadPatientInfo = async () => {
	const info = await ensurePatientCard()
	if (info && info.patientId) {
		currentPatient.value = info
	} else {
		currentPatient.value = null
	}
}

const ensurePatientId = async () => {
	if (currentPatient.value?.patientId) {
		return currentPatient.value.patientId
	}
	await loadPatientInfo()
	if (currentPatient.value?.patientId) {
		return currentPatient.value.patientId
	}
	uni.showModal({
		title: '未找到就诊卡',
		content: '请先创建并绑定就诊卡后再进行挂号支付',
		confirmText: '去创建',
		success: (res) => {
			if (res.confirm) {
				uni.navigateTo({
					url: '/subpkg/profile/personal/create-card'
				})
			}
		}
	})
	return null
}

const calculateFeeByPatientType = (type) => {
	switch (type) {
		case 1: // 学生
			return originalFee * 0.2
		case 2: // 教师
			return originalFee * 0.5
		case 3: // 职工
			return originalFee * 0.7
		default:
			return originalFee
	}
}

// 选择患者类型（仅在未锁定时允许）
const selectPatientType = (type) => {
	if (isTypeLocked.value) {
		uni.showToast({
			title: '身份类型已锁定，不可更改',
			icon: 'none'
		})
		return
	}
	patientType.value = type
	fee.value = calculateFeeByPatientType(type)
}

// 获取类型图标
const getTypeIcon = (type) => {
	switch (type) {
		case 1:
			return '🎓'
		case 2:
			return '👨‍🏫'
		case 3:
			return '👔'
		default:
			return '👤'
	}
}

onMounted(async () => {
	const pages = getCurrentPages()
	const currentPage = pages[pages.length - 1]
	const options = currentPage.options || {}
	
	// 优先使用路由参数中的 patientId
	let patientIdFromRoute = Number(options.patientId || 0)
	
	// 如果路由参数中没有 patientId，尝试从本地存储或 ensurePatientCard 获取
	if (!patientIdFromRoute) {
		try {
			const patientInfo = await ensurePatientCard()
			if (patientInfo && patientInfo.patientId) {
				patientIdFromRoute = Number(patientInfo.patientId)
			}
		} catch (e) {
			console.warn('从 ensurePatientCard 获取患者信息失败:', e)
		}
	}
	
	// 获取就诊卡信息，用于确定患者类型
	let patientCardInfo = null
	try {
		patientCardInfo = await ensurePatientCard()
		if (patientCardInfo && patientCardInfo.patientId) {
			currentPatient.value = patientCardInfo
		} else if (patientIdFromRoute) {
			currentPatient.value = {
				patientId: patientIdFromRoute
			}
		}
	} catch (e) {
		console.warn('获取就诊卡信息失败:', e)
		if (patientIdFromRoute) {
			currentPatient.value = {
				patientId: patientIdFromRoute
			}
		}
	}
	
	if (!currentPatient.value?.patientId) {
		// 如果还是没有，提示错误
		uni.showModal({
			title: '患者信息缺失',
			content: '未获取到挂号患者信息，请返回挂号页重新选择患者',
			showCancel: false,
			success: () => {
				uni.navigateBack()
			}
		})
		return
	}
	
	// 根据就诊卡的patientType自动选择并锁定身份类型
	const cardPatientType = patientCardInfo?.patientType || patientCardInfo?.patient_type
	if (cardPatientType && (cardPatientType === 1 || cardPatientType === 2 || cardPatientType === 3)) {
		// 如果就诊卡有明确的身份类型，自动选择并锁定
		patientType.value = Number(cardPatientType)
		isTypeLocked.value = true
		fee.value = calculateFeeByPatientType(Number(cardPatientType))
		console.log('根据就诊卡身份类型自动锁定:', cardPatientType)
	} else {
		// 如果没有，默认选择学生类型，但不锁定
		patientType.value = 1
		isTypeLocked.value = false
		fee.value = calculateFeeByPatientType(1)
	}

	// 其他挂号信息
	const deptParam = options.dept || ''
	const doctorParam = options.doctor || ''
	const timeParam = options.time || ''
	
	dept.value = deptParam ? decodeURIComponent(deptParam) : '未知科室'
	doctor.value = doctorParam ? decodeURIComponent(doctorParam) : '未知医生'
	time.value = timeParam ? decodeURIComponent(timeParam) : '未知时间'

	doctorId.value = Number(options.doctorId || 0)
	typeId.value = Number(options.typeId || 0)
	scheduleId.value = Number(options.scheduleId || 0)
	deptId.value = Number(options.deptId || 0)

	console.log('支付页接收参数:', {
		dept: options.dept,
		deptDecoded: dept.value,
		doctor: options.doctor,
		doctorDecoded: doctor.value,
		time: options.time,
		timeDecoded: time.value,
		doctorId: options.doctorId,
		typeId: options.typeId,
		scheduleId: options.scheduleId,
		deptId: options.deptId,
		allOptions: options
	})
})


// ------------------ 时间格式转换 ------------------
const getLocalDateTime = () => {
	const now = new Date()
	const yyyy = now.getFullYear()
	const MM = String(now.getMonth() + 1).padStart(2, '0')
	const dd = String(now.getDate()).padStart(2, '0')
	const HH = String(now.getHours()).padStart(2, '0')
	const mm = String(now.getMinutes()).padStart(2, '0')
	const ss = String(now.getSeconds()).padStart(2, '0')
	return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`
}


// ------------------ 支付 + 写入挂号 ------------------
const onPay = async () => {
	if (!selectedMethod.value) {
		uni.showToast({
			title: '请选择支付方式',
			icon: 'none'
		})
		return
	}

	const patientId = currentPatient.value?.patientId
	if (!patientId) {
		uni.showToast({
			title: '患者信息缺失',
			icon: 'none'
		})
		return
	}

	uni.showLoading({
		title: '正在支付...'
	})

	// 支付模拟：保留轻量延迟，支付成功提示放到写入成功之后
	setTimeout(async () => {
		// 构建挂号记录对象
		const record = {
			scheduleId: scheduleId.value,
			patientId,
			doctorId: doctorId.value,
			typeId: typeId.value,
			registrationNo: generateRegistrationNo(), // 前端生成或后端生成都可以
			registerTime: formatLocalDateTime(new Date()), // YYYY-MM-DD HH:mm:ss
			status: 1, // 已预约
			priceOriginal: originalFee,
			actualPrice: fee.value,
			isAdd: 0 // 正常号
		}

		let recordId = null
		let matchedRecord = null
		try {
			// 正常预约挂号不加入候补队列，第三个参数设为 false
			const result = await createRegistration(record, patientId, false)
			console.log('挂号记录写入成功', result)
			
			// 尝试从返回结果中获取记录ID
			// 后端可能返回的格式：{ result: { recordId: xxx } } 或 { data: { recordId: xxx } } 或直接返回 recordId
			if (result) {
				if (typeof result === 'object') {
					recordId = result.recordId || result.id || result.record_id || 
							   (result.result && (result.result.recordId || result.result.id || result.result.record_id)) ||
							   (result.data && (result.data.recordId || result.data.id || result.data.record_id))
				} else if (typeof result === 'number') {
					recordId = result
				}
			}

			// 成功写入后再查一次记录，拿到完整详情；若 result 未返回 recordId，则从列表兜底
			try {
				const records = await getRegistrationRecords(patientId)
				const list = Array.isArray(records) ? records : (Array.isArray(records?.data) ? records.data : [])
				// 按 registerTime 降序，匹配 scheduleId + doctorId，若未找到则取最新一条
				const scheduleIdNum = Number(record.scheduleId)
				const doctorIdNum = Number(record.doctorId)
				const filtered = list
					.filter(r => {
						const sid = Number(r.scheduleId || r.schedule_id)
						const did = Number(r.doctorId || r.doctor_id)
						return (!isNaN(scheduleIdNum) ? sid === scheduleIdNum : true) &&
							   (!isNaN(doctorIdNum) ? did === doctorIdNum : true)
					})
					.sort((a, b) => {
						const ta = new Date(a.registerTime || a.register_time || 0).getTime()
						const tb = new Date(b.registerTime || b.register_time || 0).getTime()
						return tb - ta
					})
				matchedRecord = filtered[0] || list.sort((a, b) => {
					const ta = new Date(a.registerTime || a.register_time || 0).getTime()
					const tb = new Date(b.registerTime || b.register_time || 0).getTime()
					return tb - ta
				})[0] || null

				// 如果 recordId 仍为空，尝试使用列表中的 id 兜底
				if (!recordId && matchedRecord) {
					recordId = matchedRecord.recordId || matchedRecord.record_id || matchedRecord.id
				}
			} catch (e) {
				console.warn('获取最新挂号记录失败，使用本地构建数据', e)
			}

			uni.hideLoading()
			uni.showToast({
				title: '支付成功！',
				icon: 'success',
				duration: 1200
			})
		} catch (error) {
			console.error('挂号写入失败', error)
			uni.hideLoading()

			const msg = typeof error === 'string'
				? error
				: (error?.message || error?.data?.message || '')

			if (msg.includes('您已预约过该时段，请勿重复挂号')) {
				uni.showModal({
					title: '重复预约',
					content: '该就诊人已预约过此时段，本次未生成新挂号记录。',
					showCancel: false
				})
			} else if (msg.includes('该时段预约已截止')) {
				uni.showModal({
					title: '预约已截止',
					content: '该时段预约已截止，本次未生成挂号记录，请选择其他时间段重新预约。',
					showCancel: false
				})
			} else {
				uni.showModal({
					title: '挂号失败',
					content: '支付已完成，但挂号记录写入失败，请稍后重试或联系医院工作人员处理。',
					showCancel: false
				})
			}
			return
		}

		// 跳转到挂号记录页面
		setTimeout(() => {
			// 构建记录详情数据，参考转诊自动挂号跳转
			const source = matchedRecord || {}
			const recordData = {
				id: source.recordId || source.record_id || source.id || recordId || record.recordId,
				recordId: source.recordId || source.record_id || source.id || recordId || record.recordId,
				patientId: source.patientId || source.patient_id || patientId,
				scheduleId: source.scheduleId || source.schedule_id || record.scheduleId,
				doctorId: source.doctorId || source.doctor_id || record.doctorId,
				typeId: source.typeId || source.type_id || record.typeId,
				registrationNo: source.registrationNo || source.registration_no || record.registrationNo,
				registerTime: source.registerTime || source.register_time || record.registerTime,
				status: source.status || record.status,
				priceOriginal: source.priceOriginal || source.price_original || record.priceOriginal,
				actualPrice: source.actualPrice || source.actual_price || record.actualPrice,
				isAdd: source.isAdd || source.is_add || record.isAdd || 0,
				addRemark: source.addRemark || source.add_remark || record.addRemark || ''
			}

			if (!recordData.recordId && recordId) {
				recordData.recordId = recordId
				recordData.id = recordId
			}

			// 补充挂号信息便于详情展示
			recordData.deptName = dept.value
			recordData.doctorName = doctor.value
			recordData.registerTime = recordData.registerTime || record.registerTime
			recordData.register_time = recordData.register_time || record.registerTime
			recordData.appointmentTime = time.value

			if (recordData.recordId && recordData.patientId) {
				const query = encodeURIComponent(JSON.stringify(recordData))
				// 使用 redirectTo，移除支付页出栈，返回时不再出现支付页/重复跳转
				uni.redirectTo({
					url: `/subpkg/profile/records/hospital-record-detail?record=${query}`,
					fail: () => {
						// 兜底回到挂号记录列表
						uni.redirectTo({
							url: '/subpkg/profile/records/register-record'
						})
					}
				})
			} else {
				// 没有关键字段时，回到挂号记录列表
				uni.redirectTo({
					url: '/subpkg/profile/records/register-record'
				})
			}
		}, 800)
	}, 1200)
}


// 工具函数：生成挂号单号（前端简单示例）
function generateRegistrationNo() {
	const date = new Date()
	const y = date.getFullYear()
	const m = (date.getMonth() + 1).toString().padStart(2, '0')
	const d = date.getDate().toString().padStart(2, '0')
	const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
	return `${y}${m}${d}${doctorId.value}${random}`
}

// 工具函数：格式化时间 YYYY-MM-DD HH:mm:ss
function formatLocalDateTime(date) {
	const pad = (n) => n.toString().padStart(2, '0')
	return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
</script>

<style scoped>
	.page-bg {
		background: linear-gradient(180deg, #f5f7fa 0%, #f0f3f6 100%);
		min-height: 100vh;
		padding-bottom: 120rpx;
		position: relative;
	}

	.page-bg::before {
		content: '';
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background: 
			radial-gradient(circle at 20% 20%, rgba(58, 156, 255, 0.05) 0%, transparent 50%),
			radial-gradient(circle at 80% 80%, rgba(110, 198, 255, 0.05) 0%, transparent 50%);
		pointer-events: none;
		z-index: 0;
	}

	.page-header {
		height: 120rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
		box-shadow: 0 6rpx 20rpx rgba(58, 156, 255, 0.25);
		position: relative;
		overflow: hidden;
	}

	.page-header::before {
		content: '';
		position: absolute;
		top: 0;
		left: -100%;
		width: 100%;
		height: 100%;
		background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.2) 50%, transparent 100%);
		animation: headerShine 4s infinite;
	}

	@keyframes headerShine {
		0% { left: -100%; }
		100% { left: 100%; }
	}

	.page-header .title {
		font-size: 38rpx;
		color: #fff;
		font-weight: 800;
		text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
		letter-spacing: 2rpx;
		position: relative;
		z-index: 1;
	}

	.section {
		margin: 20rpx 24rpx;
		padding: 0;
		position: relative;
		z-index: 1;
	}

	.card {
		background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
		border-radius: 24rpx;
		box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.12), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
		padding: 32rpx;
		border: 1rpx solid rgba(58, 156, 255, 0.1);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		position: relative;
		overflow: hidden;
	}

	.card::after {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 4rpx;
		background: linear-gradient(90deg, #3a9cff 0%, #6ec6ff 100%);
		opacity: 0;
		transition: opacity 0.3s ease;
	}

	.card:active {
		transform: translateY(-2rpx) scale(0.99);
		box-shadow: 0 12rpx 32rpx rgba(58, 156, 255, 0.18), 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
	}

	.card:active::after {
		opacity: 1;
	}

	.label {
		font-size: 32rpx;
		font-weight: 700;
		margin-bottom: 24rpx;
		display: block;
		color: #1f2d3d;
		position: relative;
		padding-left: 12rpx;
		letter-spacing: 0.5rpx;
	}

	.label::before {
		content: '';
		position: absolute;
		left: 0;
		top: 50%;
		transform: translateY(-50%);
		width: 6rpx;
		height: 28rpx;
		background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
		border-radius: 3rpx;
	}

	.info-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 20rpx 0;
		font-size: 28rpx;
		color: #333;
		border-bottom: 1rpx solid rgba(240, 243, 246, 0.8);
		transition: all 0.3s ease;
		position: relative;
	}

	.info-item:last-child {
		border-bottom: none;
	}

	.info-item::before {
		content: '';
		position: absolute;
		left: -32rpx;
		top: 0;
		bottom: 0;
		width: 4rpx;
		background: linear-gradient(180deg, #3a9cff 0%, #6ec6ff 100%);
		border-radius: 0 2rpx 2rpx 0;
		opacity: 0;
		transition: all 0.3s ease;
	}

	.info-item:hover {
		background: linear-gradient(90deg, rgba(58, 156, 255, 0.04) 0%, transparent 100%);
		padding-left: 12rpx;
	}

	.info-item:hover::before {
		opacity: 1;
		left: -28rpx;
	}

	.info-item text:first-child {
		color: #666;
		font-weight: 500;
		transition: color 0.3s ease;
	}

	.info-item:hover text:first-child {
		color: #3a9cff;
	}

	.price {
		color: #ff5500;
		font-weight: 700;
		font-size: 36rpx;
		text-shadow: 0 2rpx 4rpx rgba(255, 85, 0, 0.15);
		position: relative;
		padding-right: 8rpx;
	}

	.price::after {
		content: '';
		position: absolute;
		bottom: -4rpx;
		left: 0;
		right: 0;
		height: 2rpx;
		background: linear-gradient(90deg, #ff5500 0%, transparent 100%);
		opacity: 0.3;
	}

	.pay-item {
		padding: 24rpx 28rpx;
		border-radius: 20rpx;
		background: linear-gradient(135deg, #f8faff 0%, #f0f7ff 100%);
		margin-bottom: 16rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		border: 2rpx solid #e6f7ff;
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		position: relative;
		overflow: hidden;
	}

	.pay-item::before {
		content: '';
		position: absolute;
		top: 0;
		left: -100%;
		width: 100%;
		height: 100%;
		background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
		transition: left 0.5s ease;
	}

	.pay-item:active {
		transform: scale(0.97);
	}

	.pay-item:active::before {
		left: 100%;
	}

	.pay-item.selected {
		background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
		color: #fff;
		border-color: #3a9cff;
		box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.35);
		transform: translateY(-2rpx);
	}

	.pay-item.selected::before {
		display: none;
	}

	.pay-left {
		display: flex;
		align-items: center;
		font-size: 30rpx;
		font-weight: 500;
		letter-spacing: 0.5rpx;
	}

	.pay-item.selected .pay-left {
		font-weight: 600;
		text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
	}

	.icon {
		width: 40rpx;
		height: 40rpx;
		margin-right: 16rpx;
		filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
	}

	.check {
		font-size: 32rpx;
		font-weight: bold;
		text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
		animation: checkPop 0.3s ease;
	}

	.bottom-btn {
		margin: 40rpx 24rpx 0;
		position: sticky;
		bottom: 0;
		background: linear-gradient(180deg, transparent 0%, rgba(245, 247, 250, 0.95) 30%);
		padding-top: 24rpx;
		padding-bottom: 20rpx;
		z-index: 10;
		backdrop-filter: blur(10rpx);
	}

	.pay-btn {
		width: 100%;
		padding: 28rpx 0;
		background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
		color: #fff;
		font-size: 34rpx;
		font-weight: 800;
		border-radius: 24rpx;
		box-shadow: 0 10rpx 32rpx rgba(58, 156, 255, 0.4);
		transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
		letter-spacing: 2rpx;
		text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
		position: relative;
		overflow: hidden;
	}

	.pay-btn::before {
		content: '';
		position: absolute;
		top: 0;
		left: -100%;
		width: 100%;
		height: 100%;
		background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
		transition: left 0.5s ease;
	}

	.pay-btn:active {
		transform: scale(0.97);
		box-shadow: 0 6rpx 20rpx rgba(58, 156, 255, 0.3);
	}

	.pay-btn:active::before {
		left: 100%;
	}

	.pay-btn:disabled {
		background: linear-gradient(135deg, #ccc 0%, #bbb 100%);
		color: #999;
		box-shadow: none;
		text-shadow: none;
	}
	
	.patient-type-list {
	  margin-top: 16rpx;
	  display: flex;
	  flex-direction: row;
	  gap: 12rpx;
	  justify-content: space-between;
	}
	
	.patient-type-item {
	  flex: 1;
	  display: flex;
	  flex-direction: column;
	  align-items: center;
	  justify-content: center;
	  padding: 20rpx 12rpx;
	  border-radius: 16rpx;
	  background: linear-gradient(135deg, #f8faff 0%, #f0f7ff 100%);
	  border: 2rpx solid #e6f7ff;
	  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	  position: relative;
	  overflow: hidden;
	  min-height: 120rpx;
	}

	.patient-type-item::before {
		content: '';
		position: absolute;
		top: 0;
		left: -100%;
		width: 100%;
		height: 100%;
		background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.4) 50%, transparent 100%);
		transition: left 0.5s ease;
	}

	.patient-type-item:active {
	  transform: scale(0.95);
	}

	.patient-type-item:active::before {
		left: 100%;
	}
	
	.patient-type-item.selected {
	  background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
	  color: #fff;
	  border-color: #3a9cff;
	  box-shadow: 0 6rpx 20rpx rgba(58, 156, 255, 0.35);
	  transform: translateY(-2rpx);
	}

	.patient-type-item.selected::before {
		display: none;
	}
	
	.patient-type-item.disabled {
		opacity: 0.5;
		cursor: not-allowed;
		pointer-events: none;
		background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
		border-color: #d0d0d0;
	}
	
	.type-lock-tip {
		margin-top: 16rpx;
		padding: 12rpx 16rpx;
		background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
		border-radius: 12rpx;
		border: 1rpx solid #ffc107;
		font-size: 24rpx;
		color: #856404;
		text-align: center;
	}
	
	.lock-icon {
		position: absolute;
		top: 6rpx;
		right: 6rpx;
		font-size: 20rpx;
		opacity: 0.8;
	}
	
	.type-icon {
	  font-size: 32rpx;
	  margin-bottom: 8rpx;
	  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
	  transition: all 0.3s ease;
	}

	.patient-type-item.selected .type-icon {
		filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.2));
		transform: scale(1.1);
	}
	
	.patient-text {
	  font-size: 24rpx;
	  font-weight: 600;
	  letter-spacing: 0.5rpx;
	  margin-bottom: 4rpx;
	}

	.patient-type-item.selected .patient-text {
		font-weight: 700;
		text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.1);
	}
	
	.check-icon {
	  position: absolute;
	  top: 6rpx;
	  right: 6rpx;
	  font-size: 20rpx;
	  font-weight: bold;
	  color: #fff;
	  background: rgba(255, 255, 255, 0.3);
	  width: 28rpx;
	  height: 28rpx;
	  border-radius: 50%;
	  display: flex;
	  align-items: center;
	  justify-content: center;
	  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.2);
	  animation: checkPop 0.3s ease;
	}

	@keyframes checkPop {
		0% { transform: scale(0) rotate(0deg); }
		50% { transform: scale(1.2) rotate(180deg); }
		100% { transform: scale(1) rotate(360deg); }
	}

</style>