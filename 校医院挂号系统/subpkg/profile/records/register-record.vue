<template>
	<view class="page-bg">
		<!-- 标题 -->
		<view class="list-header">挂号记录</view>

		<!-- 状态筛选 Tabs -->
		<view class="status-tabs">
			<view v-for="item in filterTabs" :key="item.value"
				:class="['tab-item', activeStatus === item.value ? 'active' : '']" @click="activeStatus = item.value">
				{{ item.label }}
			</view>
		</view>


		<!-- 挂号记录列表 -->
		<view class="record-list">
			<view class="record-item" v-for="item in filteredRecords" :key="item.id" @click="cancelRecord(item)">
				<!-- 科室 + 医生 + 患者 -->
				<view class="top-row">
				  <view class="dept">{{ item.dept }}</view>
				  <view class="doctor">{{ item.doctor }}</view>
				  <view class="patient" v-if="item.patientName">({{ item.patientName }})</view>
				</view>


				<!-- 挂号类型 + 状态 -->
				<view class="row">
					<view class="tag">{{ item.type }}</view>
					<view class="status" :class="statusClassMap[item.status]">
					  {{ statusTextMap[item.status] || '未知状态' }}
					</view>
				</view>

				<!-- 挂号时间 -->
				<view class="row">
					<text class="label">挂号时间：</text>
					<text>{{ item.time }}</text>
				</view>

				<!-- 单号 -->
				<view class="row">
					<text class="label">挂号单号：</text>
					<text>{{ item.registrationNo }}</text>
				</view>

				<!-- 价格 -->
				<view class="row price-row">
					<text class="label">原价：</text>
					<text class="price-original">￥{{ item.priceOriginal }}</text>
					<text class="label">实付：</text>
					<text class="price-actual">￥{{ item.actualPrice }}</text>
				</view>
			</view>

			<view v-if="records.length === 0" class="empty-text">暂无挂号记录</view>
		</view>

		<!-- 刷新按钮 -->
		<button class="main-btn" @click="getRegisterRecords">刷新</button>
	</view>
</template>

<script setup>
	import { ref, onMounted, computed } from "vue";

	import {
		getRegistrationRecords,
		cancelRegistration,
		getDepartmentIdBySchedule,
		getPatientDetailById
	} from "@/api/registration";
	import {
		ensurePatientCard
	} from "@/utils/patientHelper";

	const records = ref([]);
	const currentPatient = ref(null);

	/** 状态映射 */
	const statusTextMap = {
	  0: "候补",
	  1: "已预约",
	  2: "已就诊",   // 2 改成 已就诊
	  3: "已退号",   // 3 改成 已退号
	  4: "已取消",
	};


	const statusClassMap = {
	  0: "pending",
	  1: "success",
	  2: "done",      // 2 → 已就诊（原来是 3）
	  3: "refund",    // 3 → 已退号（原来是 2）
	  4: "cancelled",
	};


	/** 医生映射 */
	const doctorMap = {
		1: "王医生",
		2: "张医生",
		3: "李医生",
		7: "李医生",
		8: "王医生",
		9: "李医生",
		10: "张医生",
		11: "陈医生",
		12: "吴医生",
		13: "赵医生",
		14: "孙医生",
		15: "周医生",
		16: "钱医生",
		17: "郑医生",
		18: "冯医生",
		19: "陈医生",
		20: "褚医生",
		21: "卫医生",
		22: "蒋医生",
		23: "沈医生",
		24: "韩医生",
		25: "杨医生",
		26: "韩医生",
		27: "董医生",
		28: "曹医生",
		29: "徐医生",
		30: "何医生",
		31: "罗医生",
		32: "管医生",
		33: "谢医生",
		34: "严医生",
		119: "王医生",
		120: "李医生",
		121: "张医生",
		122: "陈医生",
		123: "吴医生",
		124: "赵医生",
		125: "孙医生",
		126: "周医生",
		127: "钱医生",
		128: "郑医生",
		129: "冯医生",
		130: "陈医生",
		131: "褚医生",
		132: "卫医生",
		133: "蒋医生",
		134: "沈医生",
		135: "韩医生",
		136: "杨医生",
		137: "韩医生",
		138: "董医生",
		139: "曹医生",
		140: "徐医生",
		141: "何医生",
		142: "罗医生",
		143: "管医生",
		144: "谢医生",
		145: "严医生",
		146: "卢医生",
		147: "石医生",
		148: "高医生",
		149: "林医生",
		150: "黄医生",
		151: "徐医生",
		152: "马护士",
		153: "孔护士",
		154: "曹护士",
		155: "严护士",
		156: "王护士",
		157: "冯护士",
		158: "张护士",
		159: "陈护士",
		160: "吴护士",
		161: "刘专员",
		162: "陈专员",
		165: "钱专员",
		166: "孙专员",
		167: "周专员",
		168: "吴专员",
		169: "郑专员",
		202: "侯明昊",
		204: "测试",
	};

	/** 科室映射 */
	const scheduleMap = {
		1: "内科",
		2: "外科",
		3: "预防保健科",
		4: "体检科",
		5: "口腔科",
		6: "B超室",
		7: "护理科",
		8: "公疗报销",
		11: "呼吸内科",
		12: "心内科",
		13: "消化内科",
		14: "神经内科",
		15: "内分泌科",
		21: "骨科",
		22: "皮肤科",
		23: "普通外科",
		24: "外伤处理",
		31: "儿童保健",
		32: "妇女保健",
		33: "老年人保健",
		41: "常规体检",
		42: "入职体检",
		43: "专项体检",
		51: "口腔内科",
		52: "口腔外科",
		53: "口腔修复",
		61: "腹部B超",
		62: "妇科B超",
		63: "心脏彩超",
		71: "门诊护理",
		72: "住院护理",
		73: "社区护理",
		81: "门诊报销",
		82: "住院报销",
		83: "慢病报销",
		300: "肿瘤科",
		302: "测试",
		303: "测试2",
		304: "康复",
		305: "测试1",
	};

	/** 时间段转中文 */
	const slotText = (slot) => {
		switch (slot) {
			case "morning":
				return "上午";
			case "afternoon":
				return "下午";
			case "evening":
				return "晚上";
			default:
				return slot || "";
		}
	};

	/** 加载就诊卡 */
	const loadPatientInfo = async () => {
		const info = await ensurePatientCard();
		currentPatient.value = info?.patientId ? info : null;
		return currentPatient.value;
	};
	
	const filterTabs = [
	  { label: "全部", value: -1 },
	  { label: "候补", value: 0 },
	  { label: "已预约", value: 1 },
	  { label: "已就诊", value: 2 },
	  { label: "已退号", value: 3 }, 
	  { label: "已取消", value: 4 },
	];

	
	const activeStatus = ref(-1); // 默认全部
	
	/** 计算筛选后记录 */
	const filteredRecords = computed(() => {
	  if (activeStatus.value === -1) return records.value;
	  return records.value.filter((x) => x.status === activeStatus.value);
	});


	/** 确保 patientId 存在 */
	const ensurePatientId = async () => {
		if (currentPatient.value?.patientId) return currentPatient.value.patientId;

		await loadPatientInfo();

		if (currentPatient.value?.patientId) return currentPatient.value.patientId;

		uni.showModal({
			title: "未找到就诊卡",
			content: "请先创建就诊卡后再查看挂号记录",
			confirmText: "去创建",
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: "/subpkg/profile/personal/create-card",
					});
				}
			},
		});

		return null;
	};

	/** 挂号类型映射 */
	const registrationTypeMap = {
		1: "普通号",
		2: "专家号",
		3: "特需号",
	};
	/** 获取挂号记录 */
	const getRegisterRecords = async () => {
		const patientId = await ensurePatientId();
		if (!patientId) return;

		try {
			const res = await getRegistrationRecords(patientId);
			console.log("挂号记录 res:", res);

			// 不再过滤，只要是数组就全部处理
			const list = Array.isArray(res) ? res : [];

			records.value = await Promise.all(
				list.map(async (item) => {
					// 默认科室名
					let deptName = `科室ID: ${item.scheduleId}`;

					// 异步获取科室 ID → 科室名
					try {
						const deptId = await getDepartmentIdBySchedule(item.scheduleId);
						if (deptId) deptName = scheduleMap[deptId] || `科室ID: ${deptId}`;
					} catch (e) {
						console.error("获取科室失败", e);
					}

					// 医生名映射
					const doctorName =
						doctorMap[item.doctorId] || `医生ID: ${item.doctorId}`;

					// 挂号类型
					const typeName =
						registrationTypeMap[item.typeId] || `挂号类型ID: ${item.typeId}`;
						
						// 患者姓名
						    let patientName = '';
						    try {
						      if (item.patientId) {
						        const patient = await getPatientDetailById(item.patientId);
								console.log("请求患者详情 patientId=", item.patientId);

						        console.log("患者信息：", patient);  // 👈 加这行
						        patientName = patient?.patientName || '';

						      }
						    } catch (e) {
						      console.error("获取患者姓名失败", e);
						    }


					return {
						id: item.recordId,
						patientName,
						dept: deptName,
						doctor: `${doctorName} ${item.doctorTitle || ""}`,
						type: typeName,
						time: item.registerTime || "",
						registrationNo: item.registrationNo || "",
						status: item.status, // 0/1/2 原样返回
						priceOriginal: item.priceOriginal,
						actualPrice: item.actualPrice,
					};
				})
			);

			uni.showToast({
				title: "获取成功",
				icon: "success",
			});

		} catch (e) {
			console.error("获取挂号记录失败:", e);
			uni.showToast({
				title: "获取失败",
				icon: "none",
			});
		}
	};


	/** 取消挂号 */
	const cancelRecord = (item) => {
		uni.showModal({
			title: "取消挂号",
			content: `是否取消【${item.dept} - ${item.doctor}】的挂号？`,
			success: async (res) => {
				if (res.confirm) {
					try {
						const result = await cancelRegistration(item.id);
						console.log("取消挂号返回：", result);

						uni.showToast({
							title: "取消成功",
							icon: "success"
						});
						getRegisterRecords();
					} catch (err) {
						console.error("取消挂号失败：", err);
						uni.showToast({
							title: "取消失败",
							icon: "none"
						});
					}
				}
			},
		});
	};

	onMounted(() => getRegisterRecords());
</script>

<style scoped>
	.page-bg {
		min-height: 100vh;
		background: #f0f2f5;
		padding-bottom: 50rpx;
	}

	.list-header {
		font-size: 36rpx;
		font-weight: bold;
		padding: 32rpx 24rpx;
		color: #333;
	}

	.record-list {
		background: #fff;
		margin: 0 24rpx;
		border-radius: 16rpx;
		padding: 24rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}

	.record-item {
		border-bottom: 1rpx solid #eee;
		padding: 24rpx 0;
		transition: all 0.2s;
	}

	.record-item:last-child {
		border-bottom: none;
	}

	.record-item:hover {
		background: #f9f9f9;
	}

	.top-row {
		display: flex;
		justify-content: space-between;
		font-weight: bold;
		font-size: 32rpx;
		margin-bottom: 12rpx;
		color: #222;
	}

	.dept {
		color: #3a9cff;
	}

	.doctor {
		color: #666;
		font-size: 28rpx;
	}

	.row {
		display: flex;
		align-items: center;
		margin-bottom: 8rpx;
	}

	.label {
		color: #888;
		width: 120rpx;
		font-size: 28rpx;
	}

	.tag {
		padding: 4rpx 12rpx;
		border-radius: 12rpx;
		background: #e6f7ff;
		color: #1890ff;
		font-size: 26rpx;
		margin-right: 16rpx;
	}

	.status {
		font-size: 26rpx;
		color: #52c41a;
	}

	.status.cancelled {
		color: #f5222d;
	}

	.price-row {
		margin-top: 8rpx;
	}

	.price-original {
		color: #888;
		margin-right: 20rpx;
	}

	.price-actual {
		color: #fa541c;
		font-weight: bold;
	}

	.main-btn {
		width: 90%;
		margin: 32rpx auto;
		background: #3a9cff;
		color: #fff;
		padding: 20rpx;
		border-radius: 12rpx;
		font-size: 30rpx;
	}

	.empty-text {
		text-align: center;
		color: #999;
		padding: 48rpx 0;
		font-size: 28rpx;
	}
	
	.status-tabs {
	  display: flex;
	  background: #fff;
	  margin: 0 24rpx 20rpx;
	  padding: 16rpx 0;
	  border-radius: 12rpx;
	  overflow-x: auto;
	}
	
	.tab-item {
	  padding: 12rpx 24rpx;
	  font-size: 28rpx;
	  color: #666;
	  margin-right: 20rpx;
	  white-space: nowrap;
	}
	
	.tab-item.active {
	  color: #3a9cff;
	  font-weight: bold;
	  border-bottom: 4rpx solid #3a9cff;
	}
	
	.patient {
	  color: #f56a00;
	  font-size: 28rpx;
	  margin-left: 12rpx;
	}

	
	/* 状态颜色 */
	.status.pending { color: #faad14; }   /* 候补 */
	.status.success { color: #52c41a; }   /* 已预约 */
	.status.done { color: #13c2c2; }      /* 已就诊 */
	.status.refund { color: #fa541c; }    /* 已退号 */
	.status.cancelled { color: #f5222d; } /* 已取消 */
</style>