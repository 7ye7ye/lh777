"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "adjust-schedule",
  setup(__props) {
    const form = common_vendor.ref({
      originalSchedule: "",
      newDate: "",
      newTimePeriod: "",
      reason: "",
      contact: ""
    });
    const originalScheduleRange = common_vendor.ref([
      ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
      ["上午", "下午", "晚上"]
    ]);
    const originalScheduleValue = common_vendor.ref([0, 0]);
    const timePeriods = common_vendor.ref([
      "上午 08:00-12:00",
      "下午 14:00-17:00",
      "晚上 18:00-20:00"
    ]);
    const timePeriodIndex = common_vendor.ref(-1);
    const minDate = common_vendor.computed(() => {
      const date = /* @__PURE__ */ new Date();
      date.setDate(date.getDate() + 3);
      return formatDate(date);
    });
    const maxDate = common_vendor.computed(() => {
      const date = /* @__PURE__ */ new Date();
      date.setDate(date.getDate() + 30);
      return formatDate(date);
    });
    const formatDate = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    };
    const onOriginalScheduleChange = (e) => {
      const val = e.detail.value;
      originalScheduleValue.value = val;
      const weekday = originalScheduleRange.value[0][val[0]];
      const period = originalScheduleRange.value[1][val[1]];
      form.value.originalSchedule = `${weekday} ${period}`;
    };
    const onNewDateChange = (e) => {
      form.value.newDate = e.detail.value;
    };
    const onTimePeriodChange = (e) => {
      timePeriodIndex.value = e.detail.value;
      form.value.newTimePeriod = timePeriods.value[e.detail.value];
    };
    const validateForm = () => {
      if (!form.value.originalSchedule) {
        utils_uniHelper.uniShowToast({ title: "请选择原排班", icon: "none" });
        return false;
      }
      if (!form.value.newDate) {
        utils_uniHelper.uniShowToast({ title: "请选择新日期", icon: "none" });
        return false;
      }
      if (!form.value.newTimePeriod) {
        utils_uniHelper.uniShowToast({ title: "请选择新时间段", icon: "none" });
        return false;
      }
      if (form.value.reason.length < 10) {
        utils_uniHelper.uniShowToast({ title: "调班原因至少需要10个字", icon: "none" });
        return false;
      }
      if (!form.value.contact) {
        utils_uniHelper.uniShowToast({ title: "请输入联系方式", icon: "none" });
        return false;
      }
      if (!/^1[3-9]\d{9}$/.test(form.value.contact)) {
        utils_uniHelper.uniShowToast({ title: "请输入正确的手机号码", icon: "none" });
        return false;
      }
      return true;
    };
    const submitAdjustment = async () => {
      if (!validateForm())
        return;
      try {
        const res = await uniShowModal({
          title: "确认提交",
          content: "确定要提交调班申请吗？",
          confirmText: "确定",
          cancelText: "取消"
        });
        if (res.confirm) {
          await utils_uniHelper.uniShowToast({ title: "申请提交成功", icon: "success" });
          setTimeout(() => {
            uniNavigateBack();
          }, 1500);
        }
      } catch (error) {
        utils_uniHelper.uniShowToast({ title: "提交失败，请重试", icon: "none" });
      }
    };
    const goBackToSchedule = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
    };
    const goBack = () => {
      goBackToSchedule();
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBackToSchedule),
        b: !form.value.originalSchedule
      }, !form.value.originalSchedule ? {} : {
        c: common_vendor.t(form.value.originalSchedule)
      }, {
        d: originalScheduleRange.value,
        e: originalScheduleValue.value,
        f: common_vendor.o(onOriginalScheduleChange),
        g: !form.value.newDate
      }, !form.value.newDate ? {} : {
        h: common_vendor.t(form.value.newDate)
      }, {
        i: form.value.newDate,
        j: minDate.value,
        k: maxDate.value,
        l: common_vendor.o(onNewDateChange),
        m: timePeriodIndex.value === -1
      }, timePeriodIndex.value === -1 ? {} : {
        n: common_vendor.t(timePeriods.value[timePeriodIndex.value])
      }, {
        o: timePeriods.value,
        p: timePeriodIndex.value,
        q: common_vendor.o(onTimePeriodChange),
        r: form.value.reason,
        s: common_vendor.o(($event) => form.value.reason = $event.detail.value),
        t: common_vendor.t(form.value.reason.length),
        v: form.value.contact,
        w: common_vendor.o(($event) => form.value.contact = $event.detail.value),
        x: common_vendor.o(submitAdjustment),
        y: common_vendor.o(goBack)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4416a37b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/schedule/adjust-schedule.js.map
