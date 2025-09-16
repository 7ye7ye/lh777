<!--本页面由uniapp切片工具生成，uni-app切片-可视化设计工具(一套代码编译到7个平台iOS、Android、H5、小程序)，软件下载地址：http://www.ymznkf.com/new_view_669.htm -->
<template>
	<view class="YmContent">
			<view class="hospital_services_1">
				<view class="hospital_services_2">
				</view>
				<view class="hospital_services_3">
					<image v-on:click="hospital_services_4_4_click()"  src="/static/hospital_services/images/hospital_services_4_4.jpg" mode="scaleToFill" border="0"    class="hospital_services_4"></image>
					<text decode="true" class="hospital_services_5">尚未添加就诊人</text>
					<image v-on:click="hospital_services_6_6_click()"  src="/static/hospital_services/images/hospital_services_6_6.jpg" mode="scaleToFill" border="0"    class="hospital_services_6"></image>
				</view>
			</view>
			<view class="hospital_services_7">
				<view class="hospital_services_8">
					<view class="hospital_services_9">
					</view>
					<text decode="true" class="hospital_services_10">门诊服务</text>
				</view>
				<view class="hospital_services_11">
					<view class="hospital_services_26">
						<view class="hospital_services_28">
							<image v-on:click="hospital_services_29_29_click()"  src="/static/hospital_services/images/hospital_services_29_29.jpg" mode="scaleToFill" border="0"    class="hospital_services_29"></image>
							<text decode="true" class="hospital_services_30">AI导诊</text>
						</view>
						<view class="hospital_services_31">
							<image v-on:click="hospital_services_32_32_click()"  src="/static/hospital_services/images/hospital_services_32_32.jpg" mode="scaleToFill" border="0"    class="hospital_services_32"></image>
							<text decode="true" class="hospital_services_33">预约挂号</text>
						</view>
						<view class="hospital_services_34">
							<image v-on:click="hospital_services_35_35_click()"  src="/static/hospital_services/images/hospital_services_35_35.jpg" mode="scaleToFill" border="0"    class="hospital_services_35"></image>
							<text decode="true" class="hospital_services_36">门诊缴费</text>
						</view>
						<view class="hospital_services_37">
							<image v-on:click="hospital_services_38_38_click()"  src="/static/hospital_services/images/hospital_services_38_38.jpg" mode="scaleToFill" border="0"    class="hospital_services_38"></image>
							<text decode="true" class="hospital_services_39">报告查询</text>
						</view>
					</view>
					<view class="hospital_services_27">
						<view class="hospital_services_40">
							<image v-on:click="hospital_services_41_41_click()"  src="/static/hospital_services/images/hospital_services_41_41.jpg" mode="scaleToFill" border="0"    class="hospital_services_41"></image>
							<text decode="true" class="hospital_services_42">订单管理</text>
						</view>
						<view class="hospital_services_43">
							<image v-on:click="hospital_services_44_44_click()"  src="/static/hospital_services/images/hospital_services_44_44.jpg" mode="scaleToFill" border="0"    class="hospital_services_44"></image>
							<text decode="true" class="hospital_services_45">门诊满意度</text>
						</view>
					</view>
				</view>
				<view class="hospital_services_14">
					<view class="hospital_services_15">
					</view>
					<text decode="true" class="hospital_services_16">住院服务</text>
				</view>
				<view class="hospital_services_17">
					<view class="hospital_services_47">
						<image v-on:click="hospital_services_48_48_click()"  src="/static/hospital_services/images/hospital_services_48_48.jpg" mode="scaleToFill" border="0"    class="hospital_services_48"></image>
						<text decode="true" class="hospital_services_49">费用清单</text>
					</view>
					<view class="hospital_services_50">
						<image v-on:click="hospital_services_51_51_click()"  src="/static/hospital_services/images/hospital_services_51_51.jpg" mode="scaleToFill" border="0"    class="hospital_services_51"></image>
						<text decode="true" class="hospital_services_52">住院满意度</text>
					</view>
					<view class="hospital_services_53">
						<image v-on:click="hospital_services_54_54_click()"  src="/static/hospital_services/images/hospital_services_54_54.jpg" mode="scaleToFill" border="0"    class="hospital_services_54"></image>
						<text decode="true" class="hospital_services_55">住院小贴士</text>
					</view>
				</view>
				<view class="hospital_services_20">
					<view class="hospital_services_21">
					</view>
					<text decode="true" class="hospital_services_22">便民服务</text>
				</view>
				<view class="hospital_services_23">
					<view class="hospital_services_64">
						<image v-on:click="hospital_services_58_58_click()"  src="/static/hospital_services/images/hospital_services_58_58.jpg" mode="scaleToFill" border="0"    class="hospital_services_58"></image>
						<text decode="true" class="hospital_services_61">医院简介</text>
					</view>
					<view class="hospital_services_65">
						<image v-on:click="hospital_services_59_59_click()"  src="/static/hospital_services/images/hospital_services_59_59.jpg" mode="scaleToFill" border="0"    class="hospital_services_59"></image>
						<text decode="true" class="hospital_services_62">来院导航</text>
					</view>
				</view>
			</view>
		

		<view class="loading">{{loadingText}}</view>
		<view class="ymBbottom"></view>
	</view>
</template>

<script>
import hospital_services from "./hospital_services.js";
export default hospital_services;
</script>

<style lang="scss" scoped>
   @import './hospital_services.scss'
</style>