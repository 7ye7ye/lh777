"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const store_user = require("../../../store/user.js");
const api_doctor = require("../../../api/doctor.js");
const _sfc_main = {
  __name: "main",
  setup(__props) {
    const userStore = store_user.useUserStore();
    const doctorInfo = common_vendor.ref({
      name: "张医生",
      department: "内科"
    });
    const doctorId = common_vendor.computed(() => {
      var _a;
      return ((_a = userStore.userInfo) == null ? void 0 : _a.id) || 1;
    });
    const todayDate = common_vendor.ref("");
    const dateList = common_vendor.ref([]);
    const selectedDateIndex = common_vendor.ref(0);
    const scheduleList = common_vendor.ref([]);
    const fmtDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    const initDateList = () => {
      const dates = [];
      const weekdays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
      for (let i = 0; i < 7; i++) {
        const date = /* @__PURE__ */ new Date();
        date.setDate(date.getDate() + i);
        dates.push({
          weekday: weekdays[date.getDay()],
          day: date.getDate(),
          month: `${date.getMonth() + 1}月`,
          fullDate: date
        });
      }
      dateList.value = dates;
      const today = /* @__PURE__ */ new Date();
      todayDate.value = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`;
    };
    const selectDate = (index) => {
      selectedDateIndex.value = index;
      loadScheduleData();
    };
    const labelFromRange = (range) => {
      if (range === "08:00-12:00")
        return `上午 ${range}`;
      if (range === "14:00-17:00")
        return `下午 ${range}`;
      if (range === "18:00-20:00")
        return `晚上 ${range}`;
      return range;
    };
    const loadScheduleData = async () => {
      try {
        const sel = dateList.value[selectedDateIndex.value];
        const startDate = fmtDate(sel.fullDate);
        const resp = await api_doctor.doctorApi.getSchedules(doctorId.value, startDate, 1);
        scheduleList.value = (resp || []).map((s) => ({
          timePeriod: labelFromRange(s.timeRange),
          roomNumber: s.roomNo || "A-101",
          totalSlots: s.totalSlots || 0,
          bookedSlots: s.bookedCount || 0,
          remainingSlots: (s.totalSlots || 0) - (s.bookedCount || 0)
        }));
      } catch (e) {
        const mockData = [
          { timePeriod: "上午 08:00-12:00", roomNumber: "101", totalSlots: 20, bookedSlots: 15, remainingSlots: 5 },
          { timePeriod: "下午 14:00-17:00", roomNumber: "101", totalSlots: 15, bookedSlots: 13, remainingSlots: 2 },
          { timePeriod: "晚上 18:00-20:00", roomNumber: "102", totalSlots: 10, bookedSlots: 5, remainingSlots: 5 }
        ];
        scheduleList.value = mockData;
      }
    };
    const goToAdjustSchedule = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/apply" });
    };
    const goToPatients = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/patients/list" });
    };
    const goToProfile = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/profile/index" });
    };
    const getStatusClass = (item) => {
      const total = (item == null ? void 0 : item.totalSlots) ?? 0;
      const booked = (item == null ? void 0 : item.bookedSlots) ?? 0;
      const remaining = (item == null ? void 0 : item.remainingSlots) ?? total - booked;
      if (remaining <= 0)
        return "status-full";
      const ratio = total > 0 ? booked / total : 0;
      if (remaining < 3 || ratio >= 0.7)
        return "status-busy";
      return "status-available";
    };
    const getStatusText = (item) => {
      const cls = getStatusClass(item);
      if (cls === "status-full")
        return "已满";
      if (cls === "status-busy")
        return "紧张";
      return "可预约";
    };
    common_vendor.onMounted(() => {
      userStore.initFromStorage();
      initDateList();
      loadScheduleData();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(doctorInfo.value.name || "张医生"),
        b: common_vendor.t(doctorInfo.value.department || "内科"),
        c: common_vendor.t(todayDate.value),
        d: common_vendor.f(dateList.value, (date, index, i0) => {
          return {
            a: common_vendor.t(date.weekday),
            b: common_vendor.t(date.day),
            c: common_vendor.t(date.month),
            d: index,
            e: selectedDateIndex.value === index ? 1 : "",
            f: common_vendor.o(($event) => selectDate(index), index)
          };
        }),
        e: scheduleList.value.length === 0
      }, scheduleList.value.length === 0 ? {} : {
        f: common_vendor.f(scheduleList.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.timePeriod),
            b: common_vendor.t(getStatusText(item)),
            c: common_vendor.n(getStatusClass(item)),
            d: common_vendor.t(item.roomNumber),
            e: common_vendor.t(item.totalSlots),
            f: common_vendor.t(item.bookedSlots),
            g: common_vendor.t(item.remainingSlots),
            h: item.remainingSlots < 3 ? 1 : "",
            i: index
          };
        })
      }, {
        g: common_vendor.o(goToAdjustSchedule),
        h: common_vendor.o(goToPatients),
        i: common_vendor.o(goToProfile)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-72f5bcce"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/schedule/main.js.map
