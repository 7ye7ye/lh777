<template>
	<view class="container" v-if="receiptDetail">
		<view class="receipt-card">
            <view class="qr-code-section">
                <canvas id="qrcode" type="2d" style="width: 300rpx; height: 300rpx;"></canvas>
				<text class="visit-code">{{ receiptDetail.qrCodeData }}</text>
			</view>
			
			<view class="info-section">
				<view class="info-row">
					<text class="label">就诊人</text>
					<text class="value">{{ receiptDetail.patientName }}</text>
				</view>
				<view class="info-row">
					<text class="label">医院地址</text>
					<text class="value">{{ receiptDetail.hospitalAddress }}</text>
				</view>
				<view class="info-row">
					<text class="label">就诊科室</text>
					<text class="value">{{ receiptDetail.departmentName }}</text>
				</view>
				<view class="info-row">
					<text class="label">就诊地点</text>
					<text class="value location">{{ receiptDetail.visitLocation }}</text>
				</view>
				<view class="info-row">
					<text class="label">预约医生</text>
					<text class="value">{{ receiptDetail.doctorName }}</text>
				</view>
				<view class="info-row">
					<text class="label">预约日期</text>
					<text class="value">{{ receiptDetail.appointmentDate }}</text>
				</view>
				<view class="info-row">
					<text class="label">预约时间</text>
					<text class="value">{{ receiptDetail.appointmentTime }}</text>
				</view>
				<view class="info-row">
					<text class="label">诊查费</text>
					<text class="value price">¥ {{ receiptDetail.consultationFee }}</text>
				</view>
				<view class="info-row">
					<text class="label">业务状态</text>
					<text class="value status-success">{{ receiptDetail.status }}</text>
				</view>
				<view class="info-row">
					<text class="label">商户订单号</text>
					<text class="value">{{ receiptDetail.orderNumber }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	// 引入 uQRCode 插件，用于在前端生成二维码
	import uQRCode from '@/uni_modules/Sansnn-uQRCode/js_sdk/uqrcode/uqrcode.js'

	export default {
		data() {
			return {
				appointmentId: null,
				receiptDetail: null, // 存储回执单详情
			};
		},
		onLoad(options) {
			if (options.id) {
				this.appointmentId = options.id;
				this.fetchReceiptDetail();
			}
		},
		methods: {
			fetchReceiptDetail() {
				const apiUrl = `http://10.61.62.249:8095/jeecg-boot/api/appointment/detail`;
				
				uni.request({
					url: `${apiUrl}?id=${this.appointmentId}`,
					success: (res) => {
						if (res.statusCode === 200 && res.data) {
							this.receiptDetail = res.data;
							// 在获取到数据后，生成二维码
							this.$nextTick(() => {
								this.generateQRCode(this.receiptDetail.qrCodeData);
							});
						}
					}
				});
			},
			// 生成二维码
			// 新版本的 generateQRCode 方法
			generateQRCode(text) {
			    // 【关键修改】在 createSelectorQuery() 后面加上 .in(this)，
			    // 用于指定查询范围在当前自定义组件内，能更好地保证查询时机。
			    uni.createSelectorQuery().in(this)
			        .select('#qrcode')
			        .fields({ node: true, size: true })
			        .exec(res => {
			            // 【增加安全检查】在使用 res[0] 之前，先判断它和它的 node 属性是否存在
			            if (res && res[0] && res[0].node) {
			                const canvas = res[0].node;
			                const ctx = canvas.getContext('2d');
			                canvas.width = res[0].width;
			                canvas.height = res[0].height;
			
                            // 使用 Sansnn-uQRCode 的实例化方式
                            const qr = new uQRCode({
                                data: text,
                                size: res[0].width,
                                margin: 10,
                                backgroundColor: '#ffffff',
                                foregroundColor: '#000000'
                            }, ctx)
                            qr.make()
                            qr.drawCanvas()
			            } else {
			                // 如果还是找不到，给出一个清晰的错误提示
			                console.error("无法找到 canvas 节点，请检查 canvas-id 是否正确以及 DOM 是否已渲染。");
			                uni.showToast({
			                    title: '二维码生成失败',
			                    icon: 'none'
			                });
			            }
			        });
			}
		}
	}
</script>

<style scoped>
	.container {
		background-color: #f5f5f5;
		padding: 24rpx;
		min-height: 100vh;
	}
	.receipt-card {
		background-color: #fff;
		border-radius: 16rpx;
		overflow: hidden;
	}
	.qr-code-section {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 48rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
	}
	.visit-code {
		font-size: 32rpx;
		color: #333;
		margin-top: 16rpx;
		font-family: monospace;
	}
	.info-section {
		padding: 16rpx 32rpx 32rpx;
	}
	.info-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 16rpx 0;
		font-size: 28rpx;
	}
	.label {
		color: #999;
	}
	.value {
		color: #333;
		text-align: right;
	}
	.location {
		color: #409eff;
	}
	.price {
		color: #e4393c;
		font-weight: bold;
	}
	.status-success {
		color: #67c23a;
	}
</style>
