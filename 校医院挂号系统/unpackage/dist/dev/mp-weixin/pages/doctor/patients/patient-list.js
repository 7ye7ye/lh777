"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "patient-list",
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
    const timeSlots = common_vendor.ref(["全部", "上午", "下午"]);
    const currentTimeSlot = common_vendor.ref("全部");
    const patients = common_vendor.ref([]);
    const fetchPatients = (dateIndex, timeSlot) => {
      setTimeout(() => {
        if (dateIndex === 0) {
          let filteredPatients = [
            {
              id: 1,
              name: "张三",
              appointmentTime: "上午 9:00",
              identity: "学生",
              status: "waiting",
              age: 20,
              gender: "男",
              medicalHistory: "无特殊病史",
              previousVisits: [
                { date: "2023-05-15", department: "内科", doctor: "李医生" }
              ]
            },
            {
              id: 2,
              name: "李四",
              appointmentTime: "上午 10:30",
              identity: "教职工",
              status: "in-progress",
              age: 45,
              gender: "女",
              medicalHistory: "高血压",
              previousVisits: [
                { date: "2023-04-20", department: "内科", doctor: "李医生" },
                { date: "2023-03-10", department: "心内科", doctor: "王医生" }
              ]
            },
            {
              id: 3,
              name: "王五",
              appointmentTime: "下午 14:30",
              identity: "学生",
              status: "waiting",
              age: 22,
              gender: "男",
              medicalHistory: "过敏体质",
              previousVisits: []
            },
            {
              id: 4,
              name: "赵六",
              appointmentTime: "下午 15:45",
              identity: "教职工",
              status: "completed",
              age: 50,
              gender: "男",
              medicalHistory: "糖尿病",
              previousVisits: [
                { date: "2023-05-05", department: "内分泌科", doctor: "张医生" }
              ]
            }
          ];
          if (timeSlot === "上午") {
            filteredPatients = filteredPatients.filter((p) => p.appointmentTime.includes("上午"));
          } else if (timeSlot === "下午") {
            filteredPatients = filteredPatients.filter((p) => p.appointmentTime.includes("下午"));
          }
          patients.value = filteredPatients;
        } else if (dateIndex === 1) {
          let filteredPatients = [
            {
              id: 5,
              name: "钱七",
              appointmentTime: "上午 9:15",
              identity: "学生",
              status: "waiting",
              age: 19,
              gender: "女",
              medicalHistory: "无",
              previousVisits: []
            },
            {
              id: 6,
              name: "孙八",
              appointmentTime: "下午 16:00",
              identity: "教职工",
              status: "waiting",
              age: 35,
              gender: "男",
              medicalHistory: "胃炎",
              previousVisits: [
                { date: "2023-02-15", department: "消化内科", doctor: "刘医生" }
              ]
            }
          ];
          if (timeSlot === "上午") {
            filteredPatients = filteredPatients.filter((p) => p.appointmentTime.includes("上午"));
          } else if (timeSlot === "下午") {
            filteredPatients = filteredPatients.filter((p) => p.appointmentTime.includes("下午"));
          }
          patients.value = filteredPatients;
        } else {
          patients.value = [];
        }
      }, 300);
    };
    const selectDate = (index) => {
      currentDateIndex.value = index;
      fetchPatients(index, currentTimeSlot.value);
    };
    const selectTimeSlot = (slot) => {
      currentTimeSlot.value = slot;
      fetchPatients(currentDateIndex.value, slot);
    };
    const getStatusText = (status) => {
      switch (status) {
        case "waiting":
          return "待接诊";
        case "in-progress":
          return "接诊中";
        case "completed":
          return "已完成";
        default:
          return "未知";
      }
    };
    const viewPatientDetail = (patient) => {
      const patientInfo = encodeURIComponent(JSON.stringify(patient));
      utils_uniHelper.uniNavigateTo({ url: `/pages/doctor/patients/patient-detail?patient=${patientInfo}` });
    };
    const goBack = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
    };
    common_vendor.onMounted(() => {
      fetchPatients(0, "全部");
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.f(weekDays.value, (day, index, i0) => {
          return {
            a: common_vendor.t(day.dayOfWeek),
            b: common_vendor.t(day.date),
            c: index,
            d: currentDateIndex.value === index ? 1 : "",
            e: common_vendor.o(($event) => selectDate(index), index)
          };
        }),
        c: common_vendor.f(timeSlots.value, (slot, index, i0) => {
          return {
            a: common_vendor.t(slot),
            b: index,
            c: currentTimeSlot.value === slot ? 1 : "",
            d: common_vendor.o(($event) => selectTimeSlot(slot), index)
          };
        }),
        d: patients.value.length === 0
      }, patients.value.length === 0 ? {} : {
        e: common_vendor.f(patients.value, (patient, index, i0) => {
          return {
            a: common_vendor.t(patient.name),
            b: common_vendor.t(patient.appointmentTime),
            c: common_vendor.t(patient.identity),
            d: common_vendor.t(getStatusText(patient.status)),
            e: common_vendor.n(patient.status),
            f: index,
            g: common_vendor.o(($event) => viewPatientDetail(patient), index)
          };
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-e5915b83"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/patients/patient-list.js.map
