"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "patient-detail",
  setup(__props) {
    const patient = common_vendor.ref({});
    common_vendor.onMounted(() => {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const patientInfo = currentPage.$page.options.patient;
      if (patientInfo) {
        try {
          patient.value = JSON.parse(decodeURIComponent(patientInfo));
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/doctor/patients/patient-detail.vue:95", "解析患者信息失败", e);
        }
      }
    });
    const updateStatus = async (status) => {
      setTimeout(async () => {
        patient.value.status = status;
        let message = "";
        if (status === "in-progress") {
          message = "已标记为接诊中";
        } else if (status === "completed") {
          message = "已标记为就诊完成";
        }
        await utils_uniHelper.uniShowToast({ title: message });
      }, 500);
    };
    const goBack = () => {
      utils_uniHelper.uniNavigateBack();
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.t(patient.value.name),
        c: common_vendor.t(patient.value.age),
        d: common_vendor.t(patient.value.gender),
        e: common_vendor.t(patient.value.identity),
        f: common_vendor.t(patient.value.appointmentTime),
        g: common_vendor.t(patient.value.medicalHistory || "无"),
        h: patient.value.previousVisits && patient.value.previousVisits.length > 0
      }, patient.value.previousVisits && patient.value.previousVisits.length > 0 ? {
        i: common_vendor.f(patient.value.previousVisits, (visit, index, i0) => {
          return {
            a: common_vendor.t(visit.date),
            b: common_vendor.t(visit.department),
            c: common_vendor.t(visit.doctor),
            d: index
          };
        })
      } : {}, {
        j: patient.value.status === "completed" ? 1 : "",
        k: common_vendor.o(($event) => updateStatus("in-progress")),
        l: patient.value.status === "completed",
        m: patient.value.status !== "in-progress" ? 1 : "",
        n: common_vendor.o(($event) => updateStatus("completed")),
        o: patient.value.status !== "in-progress"
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4b29b271"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/patients/patient-detail.js.map
