"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const store_user = require("../../../store/user.js");
const api_doctor = require("../../../api/doctor.js");
const _sfc_main = {
  __name: "apply",
  setup(__props) {
    const userStore = store_user.useUserStore();
    const doctorId = common_vendor.computed(() => {
      var _a;
      return ((_a = userStore.userInfo) == null ? void 0 : _a.id) || 1;
    });
    const scheduleOptions = common_vendor.ref([]);
    const selectedScheduleId = common_vendor.ref(null);
    const selectedScheduleLabel = common_vendor.ref("");
    const newDate = common_vendor.ref("");
    const timeRanges = common_vendor.ref(["08:00-12:00", "14:00-17:00", "18:00-20:00"]);
    const newTimeRange = common_vendor.ref("");
    const reason = common_vendor.ref("");
    const goBack = () => common_vendor.index.navigateBack();
    const onScheduleChange = (e) => {
      const idx = e.detail.value;
      const opt = scheduleOptions.value[idx];
      selectedScheduleId.value = opt.id;
      selectedScheduleLabel.value = opt.label;
    };
    const onDateChange = (e) => {
      newDate.value = e.detail.value;
    };
    const onTimeChange = (e) => {
      newTimeRange.value = timeRanges.value[e.detail.value];
    };
    const submit = async () => {
      if (!selectedScheduleId.value || !newDate.value || !newTimeRange.value || !reason.value) {
        return utils_uniHelper.uniShowToast({ title: "请完整填写申请信息" });
      }
      try {
        await api_doctor.doctorApi.applyShiftChange({
          scheduleId: selectedScheduleId.value,
          newDate: newDate.value,
          newTimeRange: newTimeRange.value,
          reason: reason.value
        });
        utils_uniHelper.uniShowToast({ title: "提交成功" });
        setTimeout(() => goBack(), 500);
      } catch (e) {
        utils_uniHelper.uniShowToast({ title: "提交失败，请稍后重试" });
      }
    };
    const loadSchedules = async () => {
      try {
        const d = await api_doctor.doctorApi.getTodaySchedule(doctorId.value);
        scheduleOptions.value = d.map((s) => ({
          id: s.id,
          label: `${s.date} ${s.timeRange} | 诊室 ${s.roomNo}`
        }));
      } catch (e) {
        const today = /* @__PURE__ */ new Date();
        const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
        scheduleOptions.value = [
          { id: 1, label: `${fmt(today)} 08:00-12:00 | 诊室 A-101` },
          { id: 2, label: `${fmt(today)} 14:00-17:00 | 诊室 A-102` }
        ];
      }
    };
    common_vendor.onMounted(loadSchedules);
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.t(selectedScheduleLabel.value || "请选择原排班"),
        c: scheduleOptions.value,
        d: common_vendor.o(onScheduleChange),
        e: common_vendor.t(newDate.value || "请选择新日期"),
        f: common_vendor.o(onDateChange),
        g: common_vendor.t(newTimeRange.value || "请选择新时间段"),
        h: timeRanges.value,
        i: common_vendor.o(onTimeChange),
        j: reason.value,
        k: common_vendor.o(($event) => reason.value = $event.detail.value),
        l: common_vendor.o(submit)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-cacad53e"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/schedule/apply.js.map
