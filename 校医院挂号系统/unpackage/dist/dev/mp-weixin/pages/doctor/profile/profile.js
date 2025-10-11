"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "profile",
  setup(__props) {
    const doctorInfo = common_vendor.ref({
      name: "张医生",
      avatar: "/static/doctor.png",
      title: "主治医师",
      department: "内科",
      phone: "138****5678",
      email: "zhangdoc@hospital.com",
      licenseNumber: "110********1234",
      yearsOfPractice: 10,
      specialty: "擅长内科常见病、多发病的诊治，对呼吸系统疾病、消化系统疾病有丰富的临床经验。特别是在慢性咳嗽、慢性胃炎、高血压、糖尿病等疾病的诊疗方面具有独到见解。"
    });
    const stats = common_vendor.ref({
      totalPatients: 1856,
      todayPatients: 12,
      rating: 4.8
    });
    const showEditDialog = (field) => {
      utils_uniHelper.uniShowToast({
        title: "编辑功能开发中",
        icon: "none"
      });
    };
    const goToScheduleManagement = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
    };
    const goToStatistics = () => {
      utils_uniHelper.uniShowToast({
        title: "统计功能开发中",
        icon: "none"
      });
    };
    const goToSettings = () => {
      utils_uniHelper.uniShowToast({
        title: "设置功能开发中",
        icon: "none"
      });
    };
    const changePassword = async () => {
      utils_uniHelper.uniShowToast({
        title: "修改密码功能开发中",
        icon: "none"
      });
    };
    const logout = async () => {
      try {
        const res = await uniShowModal({
          title: "确认退出",
          content: "确定要退出登录吗？",
          confirmText: "确定",
          cancelText: "取消"
        });
        if (res.confirm) {
          await utils_uniHelper.uniShowToast({ title: "已退出登录", icon: "success" });
          setTimeout(() => {
            uniRedirectTo({ url: "/pages/login/login" });
          }, 1500);
        }
      } catch (error) {
        utils_uniHelper.uniShowToast({ title: "操作失败", icon: "none" });
      }
    };
    const goBackToSchedule = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBackToSchedule),
        b: doctorInfo.value.avatar,
        c: common_vendor.t(doctorInfo.value.name),
        d: common_vendor.t(doctorInfo.value.title),
        e: common_vendor.t(doctorInfo.value.department),
        f: common_vendor.t(stats.value.totalPatients),
        g: common_vendor.t(stats.value.todayPatients),
        h: common_vendor.t(stats.value.rating),
        i: common_vendor.t(doctorInfo.value.name),
        j: common_vendor.o(($event) => showEditDialog()),
        k: common_vendor.t(doctorInfo.value.department),
        l: common_vendor.t(doctorInfo.value.title),
        m: common_vendor.t(doctorInfo.value.phone),
        n: common_vendor.o(($event) => showEditDialog()),
        o: common_vendor.t(doctorInfo.value.email),
        p: common_vendor.o(($event) => showEditDialog()),
        q: common_vendor.t(doctorInfo.value.licenseNumber),
        r: common_vendor.t(doctorInfo.value.yearsOfPractice),
        s: common_vendor.t(doctorInfo.value.specialty),
        t: common_vendor.o(goToScheduleManagement),
        v: common_vendor.o(goToStatistics),
        w: common_vendor.o(goToSettings),
        x: common_vendor.o(changePassword),
        y: common_vendor.o(logout)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-28fa1239"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/profile/profile.js.map
