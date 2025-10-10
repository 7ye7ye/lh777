"use strict";
const common_vendor = require("../../../common/vendor.js");
const common_assets = require("../../../common/assets.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "doctor-profile",
  setup(__props) {
    const doctorInfo = common_vendor.ref({
      id: "D20230001",
      name: "张医生",
      title: "主治医师",
      department: "内科",
      specialty: "呼吸系统疾病、常见感冒、发热",
      phone: "138****1234",
      email: "zhang.doctor@hospital.edu.cn"
    });
    const stats = common_vendor.ref({
      totalPatients: 128,
      completedVisits: 120,
      scheduleChanges: 2
    });
    const logout = async () => {
      await utils_uniHelper.uniShowToast({ title: "退出成功" });
      setTimeout(() => {
        utils_uniHelper.uniNavigateTo({ url: "/pages/login/login" });
      }, 1500);
    };
    const goBack = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
    };
    common_vendor.onMounted(() => {
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_assets._imports_0$3,
        c: common_vendor.t(doctorInfo.value.name),
        d: common_vendor.t(doctorInfo.value.title),
        e: common_vendor.t(doctorInfo.value.id),
        f: common_vendor.t(doctorInfo.value.department),
        g: common_vendor.t(doctorInfo.value.specialty),
        h: common_vendor.t(doctorInfo.value.phone),
        i: common_vendor.t(doctorInfo.value.email),
        j: common_vendor.t(stats.value.totalPatients),
        k: common_vendor.t(stats.value.completedVisits),
        l: common_vendor.t(stats.value.scheduleChanges),
        m: common_vendor.o(logout)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a26a4456"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/profile/doctor-profile.js.map
