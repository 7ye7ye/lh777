"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "main",
  setup(__props) {
    const formatDate = (date) => {
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, "0");
      const dd = String(date.getDate()).padStart(2, "0");
      return `${yyyy}-${mm}-${dd}`;
    };
    const doctorInfo = common_vendor.ref({
      name: "张医生",
      department: "内科"
    });
    const todayDate = common_vendor.ref("");
    const dateList = common_vendor.ref([]);
    const selectedDateIndex = common_vendor.ref(0);
    const scheduleList = common_vendor.ref([]);
    const initDateList = () => {
      const dates = [];
      const weekdays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
      const today = /* @__PURE__ */ new Date();
      for (let i = 0; i < 7; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);
        dates.push({
          weekday: weekdays[date.getDay()],
          day: date.getDate(),
          month: `${date.getMonth() + 1}月`,
          fullDate: date,
          dateStr: formatDate(date)
          // 添加格式化后的日期字符串
        });
      }
      dateList.value = dates;
      todayDate.value = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`;
    };
    const selectDate = (index) => {
      if (selectedDateIndex.value === index)
        return;
      selectedDateIndex.value = index;
      loadScheduleData();
    };
    const TIME_SLOT_MAP = {
      // 假设 1=上午, 2=下午, 3=晚上
      1: { label: "上午 08:00-12:00", defaultTotalSlots: 20 },
      2: { label: "下午 14:00-17:00", defaultTotalSlots: 15 },
      3: { label: "晚上 18:00-20:00", defaultTotalSlots: 10 }
    };
    const normalizeSchedule = (raw) => {
      const cfg = TIME_SLOT_MAP[raw.timeSlot] || { label: raw.timeSlot || "未知时段", defaultTotalSlots: 10 };
      const total = Number(raw.totalQuota || cfg.defaultTotalSlots);
      const booked = Number(raw.usedQuota || 0);
      return {
        timePeriod: cfg.label,
        roomNumber: raw.roomNumber || (raw.deptId ? `门诊-${raw.deptId}` : "诊室"),
        // 假设后端有 roomNumber 字段
        totalSlots: total,
        bookedSlots: booked,
        remainingSlots: Math.max(total - booked, 0)
      };
    };
    const loadScheduleData = async () => {
      var _a;
      const dateStr = (_a = dateList.value[selectedDateIndex.value]) == null ? void 0 : _a.dateStr;
      if (!dateStr)
        return;
      try {
        const doctorId = 1;
        const mockRawData = [
          { timeSlot: 1, usedQuota: 5, totalQuota: 20, deptId: 20, roomNumber: "101" },
          { timeSlot: 2, usedQuota: 13, totalQuota: 15, deptId: 20, roomNumber: "101" },
          { timeSlot: 3, usedQuota: 5, totalQuota: 10, deptId: 25, roomNumber: "102" }
        ];
        let list = selectedDateIndex.value === 0 ? mockRawData : mockRawData.map((item) => ({
          ...item,
          usedQuota: Math.floor(item.usedQuota * 0.5)
          // 模拟其他日期号源较少
        }));
        scheduleList.value = Array.isArray(list) ? list.map(normalizeSchedule) : [];
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/doctor/schedule/main.vue:221", `加载 ${dateStr} 排班失败`, e);
        scheduleList.value = [];
      }
    };
    const getStatusClass = (item) => {
      const rate = item.bookedSlots / item.totalSlots;
      if (rate >= 0.9)
        return "status-full";
      if (rate >= 0.6)
        return "status-busy";
      return "status-available";
    };
    const getStatusText = (item) => {
      const rate = item.bookedSlots / item.totalSlots;
      if (rate >= 0.9)
        return "号源紧张";
      if (rate >= 0.6)
        return "预约较多";
      return "可预约";
    };
    const goToAdjustSchedule = () => {
      common_vendor.index.__f__("log", "at pages/doctor/schedule/main.vue:248", "跳转到：/pages/doctor/schedule/apply");
    };
    const goToPatients = () => {
      common_vendor.index.__f__("log", "at pages/doctor/schedule/main.vue:255", "跳转到：/pages/doctor/patients/list");
    };
    const goToProfile = () => {
      common_vendor.index.__f__("log", "at pages/doctor/schedule/main.vue:262", "跳转到：/pages/doctor/profile/index");
    };
    common_vendor.onMounted(() => {
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
