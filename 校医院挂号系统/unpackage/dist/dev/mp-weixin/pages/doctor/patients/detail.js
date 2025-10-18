"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "detail",
  setup(__props) {
    const patient = common_vendor.ref({
      name: "张**",
      gender: "男",
      age: 22,
      identity: "学生",
      phone: "138****5678",
      registrationNumber: "R2024101101",
      appointmentTime: "2024-10-11 08:00-08:30",
      department: "内科",
      doctor: "张医生",
      status: "待接诊",
      medicalHistory: [
        { type: "过敏史", description: "青霉素过敏" },
        { type: "慢性病", description: "轻度高血压（已控制）" }
      ],
      visitHistory: [
        { date: "2024-09-15", department: "内科", doctor: "李医生", diagnosis: "急性上呼吸道感染" },
        { date: "2024-07-20", department: "骨科", doctor: "王医生", diagnosis: "踝关节扭伤" }
      ]
    });
    const visitNote = common_vendor.ref("");
    const getStatusClass = (status) => {
      if (status === "待接诊")
        return "status-pending";
      if (status === "接诊中")
        return "status-progress";
      if (status === "已完成")
        return "status-done";
      return "status-pending";
    };
    function goBackToPatientList() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        common_vendor.index.navigateBack();
      } else {
        utils_uniHelper.uniNavigateTo("/pages/doctor/patients/list");
      }
    }
    function receivePatient() {
      patient.value.status = "接诊中";
      utils_uniHelper.uniShowToast("已开始接诊");
    }
    function completePatient() {
      patient.value.status = "已完成";
      utils_uniHelper.uniShowToast("已完成接诊");
    }
    function saveNote() {
      if (!visitNote.value) {
        utils_uniHelper.uniShowToast("请输入备注内容");
        return;
      }
      utils_uniHelper.uniShowToast("备注已保存");
    }
    function viewVisitDetail(visit) {
      utils_uniHelper.uniShowToast(`就诊记录：${visit.date} ${visit.department}`);
    }
    common_vendor.onLoad((options) => {
      try {
        if (options && options.patient) {
          const incoming = JSON.parse(decodeURIComponent(options.patient));
          patient.value = { ...patient.value, ...incoming };
        }
      } catch (e) {
      }
    });
    common_vendor.onMounted(() => {
      const pages = getCurrentPages();
      const cur = pages[pages.length - 1];
      if (cur && cur.getOpenerEventChannel) {
        const ec = cur.getOpenerEventChannel();
        if (ec) {
          ec.on("patient", (data) => {
            if (data)
              patient.value = { ...patient.value, ...data };
          });
          ec.on("sendPatient", (data) => {
            if (data && data.patient) {
              patient.value = { ...patient.value, ...data.patient };
            }
          });
        }
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBackToPatientList),
        b: common_vendor.t(patient.value.name),
        c: common_vendor.t(patient.value.gender),
        d: common_vendor.t(patient.value.age),
        e: common_vendor.t(patient.value.identity),
        f: common_vendor.t(patient.value.phone),
        g: common_vendor.t(patient.value.status),
        h: common_vendor.n(getStatusClass(patient.value.status)),
        i: common_vendor.t(patient.value.registrationNumber),
        j: common_vendor.t(patient.value.appointmentTime),
        k: common_vendor.t(patient.value.department),
        l: common_vendor.t(patient.value.doctor),
        m: patient.value.medicalHistory && patient.value.medicalHistory.length > 0
      }, patient.value.medicalHistory && patient.value.medicalHistory.length > 0 ? {
        n: common_vendor.f(patient.value.medicalHistory, (history, index, i0) => {
          return {
            a: common_vendor.t(history.type),
            b: common_vendor.t(history.description),
            c: index
          };
        })
      } : {}, {
        o: patient.value.visitHistory && patient.value.visitHistory.length > 0
      }, patient.value.visitHistory && patient.value.visitHistory.length > 0 ? {
        p: common_vendor.f(patient.value.visitHistory, (visit, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(visit.date),
            b: common_vendor.t(visit.department),
            c: common_vendor.t(visit.doctor),
            d: visit.diagnosis
          }, visit.diagnosis ? {
            e: common_vendor.t(visit.diagnosis)
          } : {}, {
            f: index,
            g: common_vendor.o(($event) => viewVisitDetail(visit), index)
          });
        })
      } : {}, {
        q: patient.value.status !== "已完成"
      }, patient.value.status !== "已完成" ? common_vendor.e({
        r: patient.value.status === "待接诊"
      }, patient.value.status === "待接诊" ? {
        s: common_vendor.o(receivePatient)
      } : patient.value.status === "接诊中" ? {
        v: common_vendor.o(completePatient)
      } : {}, {
        t: patient.value.status === "接诊中"
      }) : {}, {
        w: patient.value.status === "接诊中"
      }, patient.value.status === "接诊中" ? {
        x: visitNote.value,
        y: common_vendor.o(($event) => visitNote.value = $event.detail.value),
        z: common_vendor.t(visitNote.value.length),
        A: common_vendor.o(saveNote)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0529fe2b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/patients/detail.js.map
