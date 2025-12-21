<template>
	<view class="appointment-page-wrapper">
		<view class="detail-bg">
			<!-- 医生基本信息 -->
			<view class="doctor-card" v-if="doctor && doctor.doctorName">
				<image :src="doctor.avatar || '/static/doctor.svg'" mode="aspectFill" class="avatar"></image>
				<view class="doctor-info">
					<view class="name-title">
						<text class="name">{{ doctor.doctorName }}</text>
						<text class="title">{{ doctor.title }}</text>
					</view>
					<view class="specialty">擅长：{{ doctor.specialty }}</view>
				</view>
			</view>

			<!-- 科室信息 -->
			<view class="section-card" v-if="department && department.deptName">
				<view class="section-title">所属科室</view>
				<view class="section-content">{{ department.deptName }}</view>
			</view>

			<!-- 选择挂号类型 -->
			<view class="section-card">
				<view class="section-title">挂号类型</view>
				<picker mode="selector" :range="registrationTypes" range-key="typeName" @change="onTypeChange">
					<view class="picker-display">
						{{ selectedType ? selectedType.typeName : '请选择挂号类型' }}
						<text v-if="selectedType" class="price-hint">
							¥{{ selectedType.priceOriginal }}
						</text>
					</view>
				</picker>
			</view>

			<!-- 选择就诊人 -->
			<view class="section-card" v-if="patientList.length">
				<view class="section-title">就诊人</view>

				<picker mode="selector" :range="patientList" range-key="patientName" @change="onPatientChange">
					<view class="picker-display">
						<text v-if="currentPatient">
							{{ currentPatient.patientName }}
							<text class="id-hint">
								（{{ currentPatient.idCard?.slice(-4) }}）
							</text>
						</text>
						<text v-else class="placeholder">
							请选择就诊人
						</text>
					</view>
				</picker>
			</view>


			<view class="appointment-page">
				<!-- 选择预约日期 -->
				<view class="section-card">
					<view class="section-title">预约日期</view>
					<!-- 提示可预约范围 -->
					<view class="date-hint">可预约时间：七天之内</view>

					<picker mode="date" :start="today" :end="maxDate" @change="onDateChange">
						<view class="picker-display">
							{{ appointmentDate || '请选择预约日期' }}
						</view>
					</picker>
				</view>

				<!-- 选择预约时段 -->
				<view class="section-card" v-if="appointmentDate && selectedType">
					<view class="section-title">选择时段</view>
					<view v-if="loadingSchedules" class="loading-text">加载中...</view>
					<view v-else class="time-slots">
						<view v-for="slot in timeSlots" :key="slot.key" class="time-slot-item" :class="{ 
						    selected: selectedSlot === slot.key,
						    disabled: !slotStatus[slot.key]?.exists,
						    full: slotStatus[slot.key]?.exists && slotStatus[slot.key]?.remaining === 0
						  }" @click="selectTimeSlot(slot)">
							<view class="slot-info">
								<text class="slot-time">{{ slot.label }} ({{ slot.timeRange }})</text>
								<text v-if="slotStatus[slot.key]?.exists && slotStatus[slot.key]?.remaining > 0"
									class="slot-quota">
									剩余 {{ slotStatus[slot.key].remaining }}
								</text>
							</view>

							<!-- 状态显示 -->
							<view v-if="!slotStatus[slot.key]?.exists" class="slot-status none">
								无号
							</view>
							<view v-else-if="slotStatus[slot.key]?.remaining === 0" class="slot-status full">
								已满
							</view>
							<view v-else-if="selectedSlot === slot.key" class="slot-status selected">
								已选择
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 预约信息摘要 -->
		<view class="section-card summary-card" v-if="selectedSchedule && selectedType">
			<view class="section-title">预约信息</view>
			<view class="summary-item">
				<text class="summary-label">挂号类型：</text>
				<text class="summary-value">{{ selectedType.typeName }}</text>
			</view>
			<view class="summary-item">
				<text class="summary-label">预约日期：</text>
				<text class="summary-value">{{ appointmentDate }}</text>
			</view>
			<view class="summary-item">
				<text class="summary-label">预约时段：</text>
				<text class="summary-value">
					{{ selectedSchedule?.time_range || selectedSchedule?.timeRange || '' }}
				</text>
			</view>

			<view class="summary-item">
				<text class="summary-label">费用：</text>
				<text class="summary-value price">¥{{ selectedType.priceOriginal }}</text>
			</view>
		</view>

		<!-- 预约按钮 -->
		<view class="action-buttons">
			<view class="action-btn primary" :class="{ 'disabled': !canSubmit }" @click="confirmAppointment">
				<text>确认预约</text>
			</view>
		</view>

		<view class="tabbar-placeholder"></view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue'
	import {
		onLoad
	} from '@dcloudio/uni-app'
	import {
		getRegistrationTypes,
		getDoctorSchedules,
		createRegistration,
		checkDuplicateBySchedule,
		addWaitingQueue
	} from '../../api/registration'
	import {
		ensurePatientCard
	} from '@/utils/patientHelper'
	import {
		useUserStore
	} from '@/store/user'
	import {
		patientApi
	} from '../../api/patient'
	const userStore = useUserStore()
	//userStore.initFromStorage() // 从本地存储加载用户信息

	const patientList = ref([]) // 用户的所有患者
	const selectedPatientId = ref(null) // 当前选择的患者
	const selectedSlot = ref(null)
	const doctor = ref({})
	const department = ref({})
	const registrationTypes = ref([])
	const selectedType = ref(null)
	const appointmentDate = ref('')
	const schedules = ref([])
	const selectedSchedule = ref(null)
	const currentPatient = ref(null)
	const loadingSchedules = ref(false)
	const today = new Date().toISOString().split('T')[0]
	const maxDate = new Date(new Date().setDate(new Date().getDate() + 6)).toISOString().split('T')[0] // 当前日期 + 6 天 = 一周
	const timeSlots = [{
			id: 1,
			label: '上午',
			timeRange: '08:00-12:00',
			key: 'morning'
		},
		{
			id: 2,
			label: '下午',
			timeRange: '14:00-17:00',
			key: 'afternoon'
		},
		{
			id: 3,
			label: '晚上',
			timeRange: '18:00-20:00',
			key: 'evening'
		}
	]

	const timeSlotMap = {
		morning: 1,
		afternoon: 2,
		evening: 3
	}
	const timeSlotReverseMap = {
		1: 'morning',
		2: 'afternoon',
		3: 'evening'
	}

	// 过滤可用排班
	const availableSchedules = computed(() => {
		if (!appointmentDate.value || !selectedType.value || !Array.isArray(schedules.value)) return []
		return schedules.value.filter(schedule => {
			const scheduleDate = schedule.schedule_date || schedule.scheduleDate
			const scheduleTypeId = schedule.type_id || schedule.typeId
			const dateStr = scheduleDate ? String(scheduleDate).substring(0, 10) : ''
			const selectedDateStr = appointmentDate.value.substring(0, 10)
			const typeId1 = scheduleTypeId != null ? Number(scheduleTypeId) : null
			const typeId2 = selectedType.value.typeId != null ?
				Number(selectedType.value.typeId) :
				(selectedType.value.type_id != null ? Number(selectedType.value.type_id) : null)
			return dateStr === selectedDateStr && typeId1 === typeId2
		})
	})

	const canSubmit = computed(() => {
		if (!selectedType.value || !appointmentDate.value || !selectedSchedule.value) return false
		const availableQuota = Number(selectedSchedule.value.available_quota ?? selectedSchedule.value
			.availableQuota ?? 0)
		return availableQuota > 0
	})

	console.log('🔍 当前状态：', {
		appointmentDate: appointmentDate.value,
		selectedType: selectedType.value,
		selectedSchedule: selectedSchedule.value,
	})



	const userId = computed(() => userStore.userInfo?.userId)
	console.log('当前登录用户ID:', userId)

	if (!userId.value) {
	  uni.showToast({
	    title: '未登录，请先登录',
	    icon: 'none'
	  })
	}



	const loadPatientList = async () => {
	  console.log('📌 当前 userId.value =', userId.value)
	
	  if (!userId.value) return
	
	  try {
	    const res = await patientApi.getPatientList({
	      userId: userId.value
	    })
	
	    console.log('🟢 患者接口原始返回 res =', res)
	
	    // ✅ 后端直接返回数组
	    if (Array.isArray(res)) {
	      patientList.value = res
	      console.log('🟢 patientList =', patientList.value)
	
	      if (patientList.value.length) {
	        currentPatient.value = patientList.value[0]
	        selectedPatientId.value = currentPatient.value.patientId
	      }
	    } else {
	      console.error('❌ 接口返回不是数组:', res)
	    }
	
	  } catch (e) {
	    console.error('❌ 请求患者列表异常:', e)
	  }
	}



	const onPatientChange = (e) => {
		const index = e.detail.value
		currentPatient.value = patientList.value[index]
		selectedPatientId.value = currentPatient.value.patientId

		console.log('当前选择就诊人：', currentPatient.value)
	}


	const ensurePatientId = async () => {
		if (selectedPatientId.value) {
			return selectedPatientId.value
		}

		uni.showModal({
			title: '请选择就诊人',
			content: '请先选择就诊人后再进行挂号',
			showCancel: false
		})

		return null
	}


	// 页面加载
	onLoad(async (query) => {
	  console.log('📌 页面 onLoad')
	  console.log('📌 onLoad 时 userStore.userInfo =', userStore.userInfo)
	  console.log('📌 onLoad 时 userId.value =', userId.value)
	
	  await loadPatientList()
		doctor.value = {
			doctorId: query.doctorId ? Number(query.doctorId) : null,
			doctorName: query.doctorName ? decodeURIComponent(query.doctorName) : '',
			title: query.title ? decodeURIComponent(query.title) : '',
			specialty: query.specialty ? decodeURIComponent(query.specialty) : '',
			avatar: query.avatar || ''
		}
		console.log('doctorId from query:', doctor.value.doctorId)
		console.log('schedule doctorId:', schedules.value.map(s => s.doctor_id || s.doctorId))


		department.value = {
			deptId: query.deptId ? Number(query.deptId) : null,
			deptName: query.deptName ? decodeURIComponent(query.deptName) : ''
		}

		// ✅ 1. 加载挂号类型
		await loadRegistrationTypes()

		// ✅ 2. 设置默认挂号类型（比如第一个）
		if (registrationTypes.value.length > 0) {
			selectedType.value = registrationTypes.value[0]
		}

		// ✅ 3. 默认选中今天日期
		appointmentDate.value = today

		// ✅ 4. 加载排班
		if (doctor.value.doctorId) {
			await loadSchedules(doctor.value.doctorId, today, 7)
		}
	})


	// 挂号类型
	const loadRegistrationTypes = async () => {
		try {
			const res = await getRegistrationTypes()
			if (Array.isArray(res)) registrationTypes.value = res
			else if (Array.isArray(res?.result)) registrationTypes.value = res.result
			else if (Array.isArray(res?.data)) registrationTypes.value = res.data
		} catch (error) {
			console.error(error)
			uni.showToast({
				title: '加载挂号类型失败',
				icon: 'none'
			})
		}
	}

	// ------------------ 排班 ------------------
	const loadSchedules = async (doctorId, startDate, days = 7) => {
		loadingSchedules.value = true
		try {
			console.log('请求排班参数:', doctorId, startDate, days)
			const res = await getDoctorSchedules(doctorId, startDate, days)
			console.log('后端返回的完整响应：', res)

			// 提取数组
			let scheduleList = []
			if (Array.isArray(res?.result)) scheduleList = res.result
			else if (Array.isArray(res?.data)) scheduleList = res.data
			else if (Array.isArray(res)) scheduleList = res

			if (!scheduleList.length) {
				uni.showToast({
					title: '该医生暂无排班',
					icon: 'none'
				})
			}

			schedules.value = scheduleList
		} catch (e) {
			console.error('加载排班错误', e)
			schedules.value = []
			uni.showToast({
				title: '加载排班信息失败',
				icon: 'none'
			})
		} finally {
			loadingSchedules.value = false
		}
	}
	// ------------------ 计算每个时段状态 ------------------
	const slotStatus = computed(() => {
		const result = {
			morning: {
				available: false,
				remaining: 0
			},
			afternoon: {
				available: false,
				remaining: 0
			},
			evening: {
				available: false,
				remaining: 0
			}
		}

		// 没有必要 doctorId 判断，后端没返回 doctor_id 字段
		if (!appointmentDate.value || !Array.isArray(schedules.value) || !schedules.value.length) {
			return result
		}

		const selectedTypeId = Number(selectedType.value?.typeId ?? selectedType.value?.type_id ?? null)

		// ✅ 匹配日期 + 挂号类型
		const daySchedules = schedules.value.filter(s => {
			const dateStr = s.schedule_date || s.scheduleDate
			const typeId = s.type_id || s.typeId
			return (
				dateStr?.substring(0, 10) === appointmentDate.value &&
				Number(typeId) === selectedTypeId
			)
		})

		console.log('计算slotStatus时：', {
			appointmentDate: appointmentDate.value,
			selectedTypeId,
			daySchedules
		})

		daySchedules.forEach(schedule => {
			const slotKey = timeSlotReverseMap[Number(schedule.time_slot)]
			if (slotKey && schedule.status === 1) {
				const remaining = Number(schedule.available_quota ?? schedule.availableQuota ?? 0)
				result[slotKey] = {
					exists: true, // 标记有排班
					available: remaining > 0, // 有剩余才可选
					remaining
				}
			}
		})

		return result
	})



	console.log('appointmentDate:', appointmentDate.value)
	console.log('doctorId:', doctor.value?.doctorId)
	console.log('schedules (raw):', schedules.value)


	// ------------------ 选择挂号类型 ------------------
	const onTypeChange = (e) => {
		selectedType.value = registrationTypes.value[e.detail.value]
		selectedSchedule.value = null
		selectedSlot.value = null
	}

	// ------------------ 选择预约日期 ------------------
	const onDateChange = (e) => {
		appointmentDate.value = e.detail.value
		selectedSlot.value = null
		selectedSchedule.value = null
		if (doctor.value.doctorId) loadSchedules(doctor.value.doctorId, appointmentDate.value, 1)
	}

	// ------------------ 选择时段 ------------------
	const selectTimeSlot = (slot) => {
		const slotInfo = slotStatus.value[slot.key] || {
			available: false,
			remaining: 0
		}

		// 无排班不可选
		if (!slotInfo.exists) {
			uni.showToast({
				title: '该时段暂无排班',
				icon: 'none'
			})
			return
		}

		// 有排班但已满 → 可以候补
		if (slotInfo.remaining === 0) {
			// ⭐即使已满，也要找到对应 schedule，给 selectedSchedule 赋值
			const slotSchedules = schedules.value.filter(s => {
				const dateStr = s.schedule_date ?? s.scheduleDate ?? ''
				return (
					String(dateStr).substring(0, 10) === String(appointmentDate.value).substring(0, 10) &&
					Number(s.time_slot) === Number(timeSlotMap[slot.key])
				)
			})

			selectedSchedule.value = slotSchedules.length ? slotSchedules[0] : null
			console.log("已满时 selectedSchedule =", selectedSchedule.value)

			uni.showModal({
				title: '号源已满',
				content: '该时段已无可用号源，您可以选择加入候补队列',
				cancelText: '取消',
				confirmText: '加入候补',
				async success(res) {
					if (res.confirm) {
						// 检查是否有选中排班
						if (!selectedSchedule.value) {
							console.error("selectedSchedule.value 为空，无法获取排班 ID");
							uni.showToast({
								title: '候补失败：未找到排班',
								icon: 'none'
							});
							return;
						}

						try {
							const scheduleId = selectedSchedule.value.schedule_id ?? selectedSchedule.value
								.scheduleId;
							const patientId = await ensurePatientId();
							if (!patientId) return;

							const fee = 20; // 候补费用

							// 构建挂号记录对象
							const record = {
								scheduleId,
								patientId,
								doctorId: doctor.value.doctorId,
								typeId: selectedType.value.typeId,
								registrationNo: generateRegistrationNo(), // 前端生成或后端生成都可以
								registerTime: formatLocalDateTime(new Date()), // YYYY-MM-DD HH:mm:ss
								status: 0, // 候补
								priceOriginal: fee,
								actualPrice: fee,
								isAdd: 0 // 正常号
							};

							// 写入挂号记录
							const regRes = await createRegistration(record, patientId, true);
							console.log('createRegistration返回值', regRes);

							// 判断接口返回值
							if ((typeof regRes === 'string' && regRes.includes('已加入候补队列')) || regRes
								?.success) {
								selectedSlot.value = slot.key; // 更新选中状态
								uni.showToast({
									title: '已加入候补队列',
									icon: 'success'
								});
								console.log('候补挂号写入成功', {
									regRes
								});
							} else {
								// 优先显示具体错误原因
								let errorMsg = '加入候补失败';
								if (typeof regRes === 'string') errorMsg = regRes;
								else if (regRes?.message) errorMsg = `挂号记录失败：${regRes.message}`;

								uni.showToast({
									title: errorMsg,
									icon: 'none'
								});
								console.warn('候补写入部分失败', {
									regRes
								});
							}

						} catch (e) {
							console.error('加入候补异常', e);
							uni.showToast({
								title: e?.message || '加入候补失败，请稍后重试',
								icon: 'none'
							});
						}
					}
				}
			});



			return
		}

		// 正常有号源，直接选中
		selectedSlot.value = slot.key

		// 更新 selectedSchedule 对应选中时段的排班（优先选有剩余的记录，若多条选剩余最大的）
		const slotSchedules = schedules.value.filter(s => {
			const dateStr = s.schedule_date ?? s.scheduleDate ?? ''
			return (
				String(dateStr).substring(0, 10) === String(appointmentDate.value).substring(0, 10) &&
				Number(s.time_slot) === Number(timeSlotMap[slot.key])
			)
		})

		if (!slotSchedules.length) {
			selectedSchedule.value = null
		} else {
			selectedSchedule.value = slotSchedules.reduce((best, cur) => {
				const curRem = Number(cur.available_quota ?? cur.availableQuota ?? ((cur.max_quota ?? cur
					.maxQuota ?? 0) - (cur.used_quota ?? cur.usedQuota ?? 0)))
				const bestRem = best ? Number(best.available_quota ?? best.availableQuota ?? ((best
					.max_quota ?? best.maxQuota ?? 0) - (best.used_quota ?? best.usedQuota ?? 0))) : -1
				return curRem > bestRem ? cur : best
			}, null)
		}
	}


	// ------------------ 确认预约 ------------------
	const confirmAppointment = async () => {
		if (!canSubmit.value) {
			uni.showToast({
				title: '请完整选择预约信息',
				icon: 'none'
			})
			return
		}

		const scheduleId = selectedSchedule.value.schedule_id ?? selectedSchedule.value.scheduleId
		console.log("获得的 scheduleId：", scheduleId)
		console.log("selectedSchedule.value：", selectedSchedule.value)
		try {
			// 调用后端检查是否重复挂号
			const patientId = currentPatient.value?.patientId
			if (!patientId) {
			  uni.showToast({ title: '请选择就诊人', icon: 'none' })
			  return
			}

			const isDuplicate = await checkDuplicateBySchedule(patientId, selectedSchedule.value.schedule_id ||
				selectedSchedule.value.scheduleId);



			if (isDuplicate) {
				uni.showToast({
					title: '您已预约过该时段，请勿重复挂号',
					icon: 'none'
				})
				return
			}

			// 预约成功提示
			uni.showModal({
				title: '预约成功',
				content: '您的预约已成功，请前往支付完成挂号。',
				showCancel: false,
				confirmText: '去支付',
				success: () => {
					uni.navigateTo({
					      url: `/subpkg/hospital/payment?` +
					        `dept=${encodeURIComponent(department.value.deptName)}&` +
					        `deptId=${department.value.deptId}&` +
					        `doctor=${encodeURIComponent(doctor.value.doctorName)}&` +
					        `doctorId=${doctor.value.doctorId}&` +
					        `time=${encodeURIComponent(appointmentDate.value + ' ' + selectedSlot.value)}&` +
					        `typeId=${selectedType.value.typeId}&` +
					        `scheduleId=${scheduleId}&` +
					        `patientId=${patientId}`
					    })
				}
			})
			console.log('🔹 即将跳转支付页，传递的患者ID:', patientId)

		} catch (e) {
			console.error('检查重复挂号失败', e)
			uni.showToast({
				title: '无法检查重复挂号，请稍后重试',
				icon: 'none'
			})
		}
	}

	// 工具函数：生成挂号单号（前端简单示例）
	function generateRegistrationNo() {
		const date = new Date()
		const y = date.getFullYear()
		const m = (date.getMonth() + 1).toString().padStart(2, '0')
		const d = date.getDate().toString().padStart(2, '0')
		const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
		return `${y}${m}${d}${doctor.value.doctorId}${random}`
	}

	// 工具函数：格式化时间 YYYY-MM-DD HH:mm:ss
	function formatLocalDateTime(date) {
		const pad = (n) => n.toString().padStart(2, '0')
		return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
	}
</script>

<style scoped>
	.appointment-page-wrapper {
		background: #f8faff;
		min-height: 100vh;
	}

	.detail-bg {
		background: #f8faff;
		min-height: 100vh;
		padding-bottom: 180rpx;
	}

	.doctor-card {
		background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
		padding: 40rpx 24rpx;
		display: flex;
		align-items: center;
		border-radius: 16rpx;
		margin: 16rpx;
	}

	.avatar {
		width: 140rpx;
		height: 140rpx;
		border-radius: 70rpx;
		margin-right: 24rpx;
		border: 4rpx solid rgba(255, 255, 255, 0.3);
		background: #fff;
	}

	.doctor-info {
		flex: 1;
	}

	.name-title {
		display: flex;
		align-items: center;
		margin-bottom: 12rpx;
	}

	.name {
		font-size: 36rpx;
		font-weight: bold;
		color: #fff;
		margin-right: 16rpx;
	}

	.title {
		font-size: 24rpx;
		color: #fff;
		background: rgba(255, 255, 255, 0.3);
		padding: 4rpx 12rpx;
		border-radius: 4rpx;
	}

	.specialty {
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.9);
	}

	.section-card {
		background: #fff;
		border-radius: 16rpx;
		margin: 16rpx;
		padding: 24rpx;
		box-shadow: 0 4rpx 16rpx rgba(58, 156, 255, 0.08);
	}

	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 16rpx;
	}

	.section-content {
		font-size: 28rpx;
		color: #666;
	}

	.picker-display {
		font-size: 28rpx;
		color: #666;
		padding: 12rpx 0;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.price-hint {
		color: #3a9cff;
		font-weight: bold;
	}

	.date-hint {
		font-size: 24rpx;
		color: #999;
		margin-bottom: 12rpx;
	}

	.loading-text,
	.empty-text {
		text-align: center;
		padding: 40rpx 0;
		color: #999;
		font-size: 28rpx;
	}

	.time-slots {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.time-slot-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 24rpx;
		border: 2rpx solid #e0e0e0;
		border-radius: 12rpx;
		background: #fff;
		transition: all 0.3s;
	}

	.time-slot-item.selected {
		border-color: #3a9cff;
		background: #f0f7ff;
	}

	.time-slot-item.disabled {
		opacity: 0.5;
		background: #f5f5f5;
	}

	.time-slot-item:active:not(.disabled) {
		transform: scale(0.98);
	}


	.slot-info {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.slot-time {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}

	.slot-quota {
		font-size: 24rpx;
		color: #999;
	}

	.slot-status {
		font-size: 24rpx;
		padding: 8rpx 16rpx;
		border-radius: 8rpx;
	}

	.slot-status.full {
		background: #ffebee;
		color: #f44336;
	}

	.slot-status.selected {
		background: #3a9cff;
		color: #fff;
	}

	.slot-status.none {
		background: #f0f0f0;
		color: #999;
	}


	.summary-card {
		background: linear-gradient(135deg, #f0f7ff 0%, #e8f5e9 100%);
	}

	.summary-item {
		display: flex;
		justify-content: space-between;
		padding: 12rpx 0;
		font-size: 28rpx;
	}

	.summary-label {
		color: #666;
	}

	.summary-value {
		color: #333;
		font-weight: bold;
	}

	.summary-value.price {
		color: #f44336;
		font-size: 32rpx;
	}

	.action-buttons {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		display: flex;
		padding: 16rpx;
		background: #fff;
		box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
	}

	.action-btn {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 20rpx;
		border-radius: 12rpx;
		background: linear-gradient(135deg, #3a9cff 0%, #1de9b6 100%);
		color: #fff;
		font-size: 28rpx;
	}

	.action-btn.disabled {
		background: #ccc;
		opacity: 0.6;
	}

	.action-btn:active:not(.disabled) {
		opacity: 0.8;
	}

	.tabbar-placeholder {
		height: 120rpx;
	}

	.id-hint {
		font-size: 24rpx;
		color: #999;
		margin-left: 8rpx;
	}

	.placeholder {
		color: #bbb;
	}
</style>