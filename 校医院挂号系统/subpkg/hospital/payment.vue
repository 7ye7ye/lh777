<template>
	<view class="page-bg">
		<view class="page-header">
			<text class="title">挂号支付</text>
		</view>

		<view class="content-area">
			<!-- 挂号信息 -->
			<view class="section card">
				<text class="label">挂号信息</text>
				<view class="info-item"><text>科室：</text>{{ dept }}</view>
				<view class="info-item"><text>医生：</text>{{ doctor }}</view>
				<view class="info-item"><text>时间：</text>{{ time }}</view>
				<view class="info-item" v-if="registrationType">
					<text>号别类型：</text>{{ registrationType.typeName || '-' }}
				</view>
				<view class="info-item price-row">
					<view class="price-info">
						<text class="price-label">挂号定价：</text>
						<text class="price original">¥{{ originalFee.toFixed(2) }}</text>
					</view>
					<view class="price-info">
						<text class="price-label">实付金额：</text>
						<text class="price actual">¥{{ fee.toFixed(2) }}</text>
					</view>
				</view>
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
				<view class="pay-methods-row">
					<view v-for="method in paymentMethods" :key="method.value" class="pay-item-small"
						:class="{ selected: selectedMethod === method.value }" @click="selectedMethod = method.value">
						<view class="pay-left-small">
							<image v-if="method.icon" :src="method.icon" class="icon-small"></image>
							<text class="pay-text-small">{{ method.name }}</text>
						</view>
						<view v-if="selectedMethod === method.value" class="check-small">✓</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 支付按钮 -->
		<view class="bottom-btn">
			<button class="pay-btn" @click="onPay">
				立即支付
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { createRegistration, getRegistrationRecords, getRegistrationTypes } from '../../api/registration' // 挂号接口
import { ensurePatientCard } from '@/utils/patientHelper'

// ------------------ 挂号信息 ------------------
const dept = ref('')
const doctor = ref('')
const time = ref('')
const originalFee = ref(0) // 挂号原价（从号别类型获取）
const fee = ref(0) // 实际支付金额
const patientType = ref(0) // 患者类型
const isTypeLocked = ref(false) // 是否锁定身份类型
const doctorId = ref(null)
const typeId = ref(null)
const scheduleId = ref(null)
const deptId = ref(0)
const currentPatient = ref(null)
const registrationType = ref(null) // 号别类型信息

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

// 加载号别类型信息
const loadRegistrationType = async () => {
	if (!typeId.value) {
		return
	}
	
	try {
		const res = await getRegistrationTypes()
		let types = []
		if (Array.isArray(res)) {
			types = res
		} else if (Array.isArray(res?.result)) {
			types = res.result
		} else if (Array.isArray(res?.data)) {
			types = res.data
		}
		
		// 根据typeId找到对应的号别类型
		const type = types.find(t => 
			Number(t.typeId || t.type_id) === Number(typeId.value)
		)
		
		if (type) {
			registrationType.value = type
			// 获取原价（支持多种字段名）
			originalFee.value = Number(type.priceOriginal || type.price_original || 0)
			
			// 根据当前患者类型计算实际支付金额
			if (patientType.value > 0) {
				calculateFeeByPatientType(patientType.value)
			}
		}
	} catch (error) {
	}
}

const calculateFeeByPatientType = (type) => {
	if (!registrationType.value) {
		return
	}
	
	// 根据患者类型从号别类型中获取对应的价格
	switch (type) {
		case 1: // 学生
			fee.value = Number(registrationType.value.studentPrice || registrationType.value.student_price || 0)
			break
		case 2: // 教师
		case 3: // 职工
			fee.value = Number(registrationType.value.staffPrice || registrationType.value.staff_price || 0)
			break
		default:
			fee.value = originalFee.value
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
	calculateFeeByPatientType(type)
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
	
	// 先加载号别类型信息
	await loadRegistrationType()
	
	// 根据就诊卡的patientType自动选择并锁定身份类型
	const cardPatientType = patientCardInfo?.patientType || patientCardInfo?.patient_type
	if (cardPatientType && (cardPatientType === 1 || cardPatientType === 2 || cardPatientType === 3)) {
		// 如果就诊卡有明确的身份类型，自动选择并锁定
		patientType.value = Number(cardPatientType)
		isTypeLocked.value = true
		calculateFeeByPatientType(Number(cardPatientType))
	} else {
		// 如果没有，默认选择学生类型，但不锁定
		patientType.value = 1
		isTypeLocked.value = false
		calculateFeeByPatientType(1)
	}
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
		uni.showModal({
			title: '提示',
			content: '请先选择支付方式',
			showCancel: false,
			confirmText: '知道了'
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
		// 确保金额是数字类型，并保留两位小数
		const actualPrice = Number(fee.value.toFixed(2))
		
		// 构建挂号记录对象
		// 注意：后端会根据患者类型从号别类型中重新计算actualPrice，所以这里传的值可能被覆盖
		// 但为了保持一致性，我们仍然传递计算好的值
		const record = {
			scheduleId: scheduleId.value,
			patientId,
			doctorId: doctorId.value,
			typeId: typeId.value,
			registrationNo: generateRegistrationNo(), // 前端生成或后端生成都可以
			registerTime: formatLocalDateTime(new Date()), // YYYY-MM-DD HH:mm:ss
			status: 1, // 已预约
			priceOriginal: originalFee.value, // 使用从号别类型获取的原价
			actualPrice: actualPrice, // 使用计算后的实际支付金额
			isAdd: 0 // 正常号
		}

		let recordId = null
		let matchedRecord = null
		try {
			// 正常预约挂号不加入候补队列，第三个参数设为 false
			const result = await createRegistration(record, patientId, false)
			
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
			}

			uni.hideLoading()
			uni.showToast({
				title: '支付成功！',
				icon: 'success',
				duration: 1200
			})
		} catch (error) {
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
				priceOriginal: source.priceOriginal || source.price_original || record.priceOriginal || originalFee.value,
				actualPrice: source.actualPrice || source.actual_price || record.actualPrice || fee.value,
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
		background: #f8faff;
		height: 100vh;
		overflow: hidden;
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		flex-direction: column;
	}

	.content-area {
		flex: 1;
		overflow: hidden;
		display: flex;
		flex-direction: column;
		padding-bottom: 0;
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
		height: 100rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #3a9cff;
		position: relative;
		overflow: hidden;
		flex-shrink: 0;
		padding-top: calc(env(safe-area-inset-top));
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
		font-size: 36rpx;
		color: #fff;
		font-weight: bold;
		letter-spacing: 1rpx;
		position: relative;
		z-index: 1;
	}

	.section {
		margin: 12rpx 24rpx;
		padding: 0;
		position: relative;
		z-index: 1;
		flex-shrink: 0;
	}

	.card {
		background: #fff;
		border-radius: 16rpx;
		box-shadow: 0 4rpx 16rpx rgba(58,156,255,0.08);
		padding: 24rpx;
		transition: all 0.3s ease;
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
		font-size: 30rpx;
		font-weight: bold;
		margin-bottom: 16rpx;
		display: block;
		color: #333;
		position: relative;
		padding-left: 12rpx;
		letter-spacing: 0.3rpx;
	}

	.label::before {
		content: '';
		position: absolute;
		left: 0;
		top: 50%;
		transform: translateY(-50%);
		width: 4rpx;
		height: 24rpx;
		background: #3a9cff;
		border-radius: 2rpx;
	}

	.info-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 12rpx 0;
		font-size: 26rpx;
		color: #333;
		border-bottom: 1rpx solid #f0f0f0;
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

	.price-row {
		flex-direction: column;
		align-items: flex-start;
		gap: 10rpx;
		padding: 12rpx 0;
		margin-top: 8rpx;
	}

	.price-info {
		display: flex;
		justify-content: space-between;
		align-items: center;
		width: 100%;
	}

	.price-label {
		color: #666;
		font-size: 26rpx;
		font-weight: 500;
	}

	.price {
		font-weight: 700;
		position: relative;
	}

	.price.original {
		color: #999;
		text-decoration: line-through;
		font-size: 24rpx;
	}

	.price.actual {
		color: #ff5500;
		font-size: 32rpx;
	}

	.pay-methods-row {
		display: flex;
		gap: 10rpx;
		justify-content: space-between;
		margin-top: 12rpx;
	}

	.pay-item-small {
		flex: 1;
		padding: 14rpx 8rpx;
		border-radius: 12rpx;
		background: #f8faff;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		border: 2rpx solid #e6f7ff;
		transition: all 0.3s ease;
		position: relative;
		overflow: hidden;
		min-height: 90rpx;
	}

	.pay-item-small::before {
		content: '';
		position: absolute;
		top: 0;
		left: -100%;
		width: 100%;
		height: 100%;
		background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);
		transition: left 0.5s ease;
	}

	.pay-item-small:active {
		transform: scale(0.95);
	}

	.pay-item-small:active::before {
		left: 100%;
	}

	.pay-item-small.selected {
		background: #3a9cff;
		color: #fff;
		border-color: #3a9cff;
		box-shadow: 0 2rpx 12rpx rgba(58, 156, 255, 0.3);
	}

	.pay-item-small.selected::before {
		display: none;
	}

	.pay-left-small {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
	}

	.pay-item-small.selected .pay-left-small {
		font-weight: 600;
		text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
	}

	.icon-small {
		width: 32rpx;
		height: 32rpx;
		filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
	}

	.pay-text-small {
		font-size: 20rpx;
		font-weight: 500;
		letter-spacing: 0.2rpx;
		text-align: center;
	}

	.pay-item-small.selected .pay-text-small {
		font-weight: 600;
		text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.1);
	}

	.check-small {
		position: absolute;
		top: 4rpx;
		right: 4rpx;
		font-size: 20rpx;
		font-weight: bold;
		color: #fff;
		background: rgba(255, 255, 255, 0.3);
		width: 24rpx;
		height: 24rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.2);
		animation: checkPop 0.3s ease;
	}

	.bottom-btn {
		margin: 16rpx 24rpx;
		margin-bottom: calc(16rpx + env(safe-area-inset-bottom));
		position: relative;
		z-index: 10;
		flex-shrink: 0;
	}

	.pay-btn {
		width: 100%;
		padding: 24rpx 0;
		background: linear-gradient(135deg, #3a9cff 0%, #6ec6ff 100%);
		color: #fff;
		font-size: 30rpx;
		font-weight: 700;
		border-radius: 24rpx;
		box-shadow: 0 8rpx 24rpx rgba(58, 156, 255, 0.4);
		transition: all 0.3s ease;
		letter-spacing: 1rpx;
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
	  margin-top: 12rpx;
	  display: flex;
	  flex-direction: row;
	  gap: 10rpx;
	  justify-content: space-between;
	}
	
	.patient-type-item {
	  flex: 1;
	  display: flex;
	  flex-direction: column;
	  align-items: center;
	  justify-content: center;
	  padding: 16rpx 8rpx;
	  border-radius: 12rpx;
	  background: #f8faff;
	  border: 2rpx solid #e6f7ff;
	  transition: all 0.3s ease;
	  position: relative;
	  overflow: hidden;
	  min-height: 100rpx;
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
	  background: #3a9cff;
	  color: #fff;
	  border-color: #3a9cff;
	  box-shadow: 0 2rpx 12rpx rgba(58, 156, 255, 0.3);
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
		margin-top: 12rpx;
		padding: 10rpx 12rpx;
		background: #fff3cd;
		border-radius: 8rpx;
		border: 1rpx solid #ffc107;
		font-size: 22rpx;
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