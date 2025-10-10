"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "apply-change",
  setup(__props) {
    const getTodayString = () => {
      const today2 = /* @__PURE__ */ new Date();
      return today2.toISOString().split("T")[0];
    };
    const getEndDateString = () => {
      const end = /* @__PURE__ */ new Date();
      end.setDate(end.getDate() + 30);
      return end.toISOString().split("T")[0];
    };
    const today = getTodayString();
    const endDate = getEndDateString();
    const originalSchedules = common_vendor.ref([
      { id: 1, date: "2023-06-10", timeSlot: "上午 8:00-11:30", roomNumber: "A-101", display: "2023-06-10 上午 (A-101)" },
      { id: 2, date: "2023-06-10", timeSlot: "下午 14:00-17:30", roomNumber: "A-102", display: "2023-06-10 下午 (A-102)" },
      { id: 3, date: "2023-06-11", timeSlot: "上午 8:00-11:30", roomNumber: "B-201", display: "2023-06-11 上午 (B-201)" },
      { id: 4, date: "2023-06-13", timeSlot: "下午 14:00-17:30", roomNumber: "A-103", display: "2023-06-13 下午 (A-103)" }
    ]);
    const timeSlots = common_vendor.ref(["上午 8:00-11:30", "下午 14:00-17:30"]);
    const selectedOriginalSchedule = common_vendor.ref(null);
    const newDate = common_vendor.ref("");
    const selectedTimeSlot = common_vendor.ref("");
    const reason = common_vendor.ref("");
    const onOriginalScheduleChange = (e) => {
      selectedOriginalSchedule.value = originalSchedules.value[e.detail.value];
    };
    const onNewDateChange = (e) => {
      newDate.value = e.detail.value;
    };
    const onTimeSlotChange = (e) => {
      selectedTimeSlot.value = timeSlots.value[e.detail.value];
    };
    const isFormValid = common_vendor.computed(() => {
      return selectedOriginalSchedule.value && newDate.value && selectedTimeSlot.value && reason.value.trim().length > 0;
    });
    const submitApplication = async () => {
      if (!isFormValid.value) {
        await utils_uniHelper.uniShowToast({ title: "请填写完整信息", icon: "none" });
        return;
      }
      setTimeout(async () => {
        await utils_uniHelper.uniShowToast({ title: "申请提交成功" });
        setTimeout(() => {
          utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
        }, 1500);
      }, 1e3);
    };
    const goBack = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.t(selectedOriginalSchedule.value ? selectedOriginalSchedule.value.display : "请选择原排班时间"),
        c: originalSchedules.value,
        d: common_vendor.o(onOriginalScheduleChange),
        e: common_vendor.t(newDate.value || "请选择新日期"),
        f: newDate.value,
        g: common_vendor.unref(today),
        h: common_vendor.unref(endDate),
        i: common_vendor.o(onNewDateChange),
        j: common_vendor.t(selectedTimeSlot.value || "请选择新时间段"),
        k: timeSlots.value,
        l: common_vendor.o(onTimeSlotChange),
        m: reason.value,
        n: common_vendor.o(($event) => reason.value = $event.detail.value),
        o: common_vendor.t(reason.value.length),
        p: !isFormValid.value,
        q: common_vendor.o(submitApplication)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-8acbf54a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/schedule/apply-change.js.map
