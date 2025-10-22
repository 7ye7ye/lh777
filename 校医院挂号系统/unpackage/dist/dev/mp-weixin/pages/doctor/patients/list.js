"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const store_user = require("../../../store/user.js");
const api_doctor = require("../../../api/doctor.js");
const _sfc_main = {
  __name: "list",
  setup(__props) {
    const userStore = store_user.useUserStore();
    const doctorId = common_vendor.computed(() => {
      var _a;
      return ((_a = userStore.userInfo) == null ? void 0 : _a.id) || 1;
    });
    const selectedDate = common_vendor.ref("");
    const timeRanges = common_vendor.ref(["全部", "08:00-12:00", "14:00-17:00", "18:00-20:00"]);
    const selectedSlot = common_vendor.ref("全部");
    const patients = common_vendor.ref([]);
    const goBack = () => common_vendor.index.navigateBack();
    const onDateChange = async (e) => {
      selectedDate.value = e.detail.value;
      await loadPatients();
    };
    const onTimeChange = (e) => {
      selectedSlot.value = timeRanges.value[e.detail.value];
    };
    const filteredPatients = common_vendor.computed(() => {
      if (selectedSlot.value === "全部")
        return patients.value;
      return patients.value.filter((p) => p.appointmentTimeRange === selectedSlot.value);
    });
    const loadPatients = async () => {
      try {
        patients.value = await api_doctor.doctorApi.getPatientsByDate(doctorId.value, selectedDate.value);
        if (!Array.isArray(patients.value))
          patients.value = [];
      } catch (e) {
        patients.value = [
          { appointmentId: 101, patientId: 1001, name: "张三", identity: "学生", appointmentTimeRange: "08:00-12:00", statusText: "已预约", statusClass: "status-wait" },
          { appointmentId: 102, patientId: 1002, name: "李四", identity: "教职工", appointmentTimeRange: "14:00-17:00", statusText: "已预约", statusClass: "status-wait" }
        ];
      }
    };
    const openDetail = (p) => {
      const qs = `?id=${p.patientId}&appointmentId=${p.appointmentId}`;
      utils_uniHelper.uniNavigateTo({ url: `/pages/doctor/patients/detail${qs}` });
    };
    common_vendor.onMounted(async () => {
      const today = /* @__PURE__ */ new Date();
      const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
      selectedDate.value = fmt(today);
      await loadPatients();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.t(selectedDate.value || "请选择日期"),
        c: common_vendor.o(onDateChange),
        d: common_vendor.t(selectedSlot.value || "全部"),
        e: timeRanges.value,
        f: common_vendor.o(onTimeChange),
        g: filteredPatients.value.length === 0
      }, filteredPatients.value.length === 0 ? {} : {
        h: common_vendor.f(filteredPatients.value, (p, k0, i0) => {
          return {
            a: common_vendor.t(p.name),
            b: common_vendor.t(p.identity),
            c: common_vendor.t(p.appointmentTimeRange),
            d: common_vendor.t(p.statusText),
            e: common_vendor.n(p.statusClass),
            f: p.appointmentId,
            g: common_vendor.o(($event) => openDetail(p), p.appointmentId)
          };
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-102359a3"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/patients/list.js.map
