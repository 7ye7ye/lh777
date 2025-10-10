"use strict";
const common_vendor = require("../../../common/vendor.js");
const common_assets = require("../../../common/assets.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "schedule",
  setup(__props) {
    const generateWeekDays = () => {
      const days = [];
      const dayNames = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
      for (let i = 0; i < 7; i++) {
        const date = /* @__PURE__ */ new Date();
        date.setDate(date.getDate() + i);
        days.push({
          date: `${date.getMonth() + 1}/${date.getDate()}`,
          fullDate: date.toISOString().split("T")[0],
          dayOfWeek: dayNames[date.getDay()]
        });
      }
      return days;
    };
    const weekDays = common_vendor.ref(generateWeekDays());
    const currentDateIndex = common_vendor.ref(0);
    const schedules = common_vendor.ref([]);
    const fetchSchedules = (dateIndex) => {
      setTimeout(() => {
        if (dateIndex === 0) {
          schedules.value = [
            {
              id: 1,
              date: weekDays.value[dateIndex].fullDate,
              timeSlot: "上午 8:00-11:30",
              roomNumber: "A-101",
              bookedCount: 15,
              totalCount: 20
            },
            {
              id: 2,
              date: weekDays.value[dateIndex].fullDate,
              timeSlot: "下午 14:00-17:30",
              roomNumber: "A-102",
              bookedCount: 8,
              totalCount: 20
            }
          ];
        } else if (dateIndex === 1) {
          schedules.value = [
            {
              id: 3,
              date: weekDays.value[dateIndex].fullDate,
              timeSlot: "上午 8:00-11:30",
              roomNumber: "B-201",
              bookedCount: 5,
              totalCount: 20
            }
          ];
        } else if (dateIndex === 2) {
          schedules.value = [];
        } else {
          schedules.value = [
            {
              id: 4,
              date: weekDays.value[dateIndex].fullDate,
              timeSlot: "下午 14:00-17:30",
              roomNumber: "A-103",
              bookedCount: 12,
              totalCount: 15
            }
          ];
        }
      }, 300);
    };
    const selectDate = (index) => {
      currentDateIndex.value = index;
      fetchSchedules(index);
    };
    const getStatusText = (item) => {
      const remaining = item.totalCount - item.bookedCount;
      if (remaining === 0)
        return "已满";
      if (remaining <= 5)
        return "紧张";
      return "充足";
    };
    const getStatusClass = (item) => {
      const remaining = item.totalCount - item.bookedCount;
      if (remaining === 0)
        return "full";
      if (remaining <= 5)
        return "tight";
      return "available";
    };
    const goToApplyChange = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/apply-change" });
    };
    const goToPatientList = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/patients/patient-list" });
    };
    const goToProfile = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/profile/doctor-profile" });
    };
    common_vendor.onMounted(() => {
      fetchSchedules(0);
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(weekDays.value, (day, index, i0) => {
          return {
            a: common_vendor.t(day.dayOfWeek),
            b: common_vendor.t(day.date),
            c: index,
            d: currentDateIndex.value === index ? 1 : "",
            e: common_vendor.o(($event) => selectDate(index), index)
          };
        }),
        b: schedules.value.length === 0
      }, schedules.value.length === 0 ? {} : {
        c: common_vendor.f(schedules.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.timeSlot),
            b: common_vendor.t(item.roomNumber),
            c: common_vendor.t(item.bookedCount),
            d: common_vendor.t(item.totalCount),
            e: common_vendor.t(getStatusText(item)),
            f: common_vendor.n(getStatusClass(item)),
            g: index
          };
        })
      }, {
        d: common_vendor.o(goToApplyChange),
        e: common_assets._imports_0$2,
        f: common_vendor.o(goToPatientList),
        g: common_assets._imports_0$3,
        h: common_vendor.o(goToProfile)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c79782dd"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/schedule/schedule.js.map
