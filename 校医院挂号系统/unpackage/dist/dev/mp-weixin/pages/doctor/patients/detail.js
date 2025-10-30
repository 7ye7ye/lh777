"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const api_doctor = require("../../../api/doctor.js");
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
    const patientIdRef = common_vendor.ref(null);
    const appointmentIdRef = common_vendor.ref(null);
    const calcAge = (birthDate) => {
      if (!birthDate)
        return "";
      try {
        const d = new Date(birthDate);
        const now = /* @__PURE__ */ new Date();
        let age = now.getFullYear() - d.getFullYear();
        const m = now.getMonth() - d.getMonth();
        if (m < 0 || m === 0 && now.getDate() < d.getDate())
          age--;
        return age;
      } catch {
        return "";
      }
    };
    const mapIdentity = (type) => {
      if (type === 1)
        return "学生";
      if (type === 2)
        return "教师";
      if (type === 3)
        return "职工";
      return "其他";
    };
    async function loadPatientDetail(id) {
      var _a, _b, _c;
      try {
        const detail = await api_doctor.doctorApi.getPatientDetail(id);
        const p = (detail == null ? void 0 : detail.patient) || {};
        const visits = Array.isArray(detail == null ? void 0 : detail.visits) ? detail.visits : [];
        const statusMap = (s) => s === 2 ? "已完成" : s === 1 ? "接诊中" : "待接诊";
        const latestStatus = visits.length > 0 ? statusMap((_a = visits[0]) == null ? void 0 : _a.status) : "待接诊";
        patient.value = {
          name: p.patientName || p.name || "",
          gender: p.gender || "",
          age: calcAge(p.birthDate),
          identity: mapIdentity(p.patientType),
          phone: p.phone || "",
          registrationNumber: ((_b = visits[0]) == null ? void 0 : _b.visitNo) || (appointmentIdRef.value ? String(appointmentIdRef.value) : ""),
          appointmentTime: ((_c = visits[0]) == null ? void 0 : _c.timeSlot) || "",
          department: "",
          // 后端当前VO未返回科室名（只有 deptId），先留空
          doctor: "",
          // 同理 doctorId -> 名称未返回，先留空
          status: latestStatus,
          medicalHistory: [],
          // 暂无病史字段，留空
          visitHistory: visits.map((v) => ({
            date: v.visitDate,
            department: "",
            // 仅有 deptId，页面先留空
            doctor: "",
            // 仅有 doctorId，页面先留空
            diagnosis: v.diagnosis || ""
          }))
        };
      } catch (e) {
        utils_uniHelper.uniShowToast({ title: "获取患者详情失败", icon: "none" });
      }
    }
    function goBackToPatientList() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        common_vendor.index.navigateBack();
      } else {
        utils_uniHelper.uniNavigateTo("/pages/doctor/patients/list");
      }
    }
    async function receivePatient() {
      if (!appointmentIdRef.value) {
        utils_uniHelper.uniShowToast({ title: "缺少预约ID", icon: "none" });
        return;
      }
      try {
        await api_doctor.doctorApi.updatePatientStatus(appointmentIdRef.value, "start");
        patient.value.status = "接诊中";
        utils_uniHelper.uniShowToast({ title: "已开始接诊", icon: "success" });
      } catch {
        utils_uniHelper.uniShowToast({ title: "开始接诊失败", icon: "none" });
      }
    }
    async function completePatient() {
      if (!appointmentIdRef.value) {
        utils_uniHelper.uniShowToast({ title: "缺少预约ID", icon: "none" });
        return;
      }
      try {
        await api_doctor.doctorApi.updatePatientStatus(appointmentIdRef.value, "finish");
        patient.value.status = "已完成";
        utils_uniHelper.uniShowToast({ title: "已完成接诊", icon: "success" });
      } catch {
        utils_uniHelper.uniShowToast({ title: "完成接诊失败", icon: "none" });
      }
    }
    function saveNote() {
      if (!visitNote.value) {
        utils_uniHelper.uniShowToast({ title: "请输入备注内容", icon: "none" });
        return;
      }
      utils_uniHelper.uniShowToast({ title: "备注已保存", icon: "success" });
    }
    function viewVisitDetail(visit) {
      utils_uniHelper.uniShowToast({ title: `就诊记录：${visit.date}`, icon: "none" });
    }
    common_vendor.onLoad((options) => {
      try {
        if (options && options.patient) {
          const incoming = JSON.parse(decodeURIComponent(options.patient));
          patient.value = { ...patient.value, ...incoming };
        }
      } catch (e) {
      }
      if (options == null ? void 0 : options.id) {
        patientIdRef.value = Number(options.id);
      }
      if (options == null ? void 0 : options.appointmentId) {
        appointmentIdRef.value = Number(options.appointmentId);
      }
    });
    common_vendor.onMounted(async () => {
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
            if (data == null ? void 0 : data.patient) {
              patient.value = { ...patient.value, ...data.patient };
            }
          });
        }
      }
      if (patientIdRef.value) {
        await loadPatientDetail(patientIdRef.value);
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
