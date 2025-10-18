"use strict";
const common_vendor = require("../../common/vendor.js");
const api_department = require("../../api/department.js");
const _sfc_main = {
  __name: "department-detail",
  setup(__props) {
    const deptId = common_vendor.ref("");
    const department = common_vendor.ref({});
    const doctorList = common_vendor.ref([]);
    const loadDepartmentDetail = async () => {
      try {
        const res = await api_department.getDepartmentDetail(deptId.value);
        common_vendor.index.__f__("log", "at subpkg/hospital/department-detail.vue:59", "科室详情数据:", res);
        let data = res;
        if (res && res.data) {
          data = res.data;
        } else if (res && res.result) {
          data = res.result;
        }
        if (data) {
          department.value = data;
        } else {
          common_vendor.index.__f__("warn", "at subpkg/hospital/department-detail.vue:72", "科室详情数据格式异常:", res);
          common_vendor.index.showToast({
            title: "数据格式异常",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at subpkg/hospital/department-detail.vue:79", "加载科室详情失败:", error);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      }
    };
    const loadDoctorsByDeptId = async () => {
      doctorList.value = [
        {
          doctorId: 1,
          doctorName: "张医生",
          title: "主任医师",
          specialty: "心血管疾病诊疗",
          avatar: "/static/doctor.png"
        },
        {
          doctorId: 2,
          doctorName: "李医生",
          title: "副主任医师",
          specialty: "呼吸系统疾病",
          avatar: "/static/doctor.png"
        }
      ];
    };
    const navigateToDoctorDetail = (doctor) => {
      common_vendor.index.navigateTo({
        url: `/pages/doctor/detail?doctorId=${doctor.doctorId}`
      });
    };
    common_vendor.onLoad((query) => {
      deptId.value = (query == null ? void 0 : query.deptId) || "";
      if (deptId.value) {
        loadDepartmentDetail();
        loadDoctorsByDeptId();
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(department.value.deptName),
        b: common_vendor.t(department.value.deptDesc || "暂无介绍"),
        c: department.value.location
      }, department.value.location ? {
        d: common_vendor.t(department.value.location)
      } : {}, {
        e: common_vendor.f(doctorList.value, (doctor, index, i0) => {
          return {
            a: doctor.avatar || "/static/images/default-avatar.png",
            b: common_vendor.t(doctor.doctorName),
            c: common_vendor.t(doctor.title),
            d: common_vendor.t(doctor.specialty),
            e: index,
            f: common_vendor.o(($event) => navigateToDoctorDetail(doctor), index)
          };
        }),
        f: doctorList.value.length === 0
      }, doctorList.value.length === 0 ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b44663a0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subpkg/hospital/department-detail.js.map
