"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_user = require("../../api/user.js");
const store_user = require("../../store/user.js");
const utils_uniHelper = require("../../utils/uniHelper.js");
const _sfc_main = {
  __name: "profile",
  setup(__props) {
    const userInfo = common_vendor.ref({});
    const userStore = store_user.useUserStore();
    const getUserInfo = () => {
      if (userStore.userInfo) {
        userInfo.value = userStore.userInfo;
      } else {
        api_user.userApi.getCurrentUser().then((res) => {
          userInfo.value = res.data;
        }).catch(() => {
          userInfo.value = { name: "微信用户", phone: "***********" };
        });
      }
    };
    const goToMyCard = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/personal/mycard" });
    };
    const goToMyPatient = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/personal/mypatient" });
    };
    const goToMyDoctor = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/personal/mydoctor" });
    };
    const goToRegisterRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/records/register-record" });
    };
    const goToOutpatientRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/records/outpatient-record" });
    };
    const goToHospitalRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/records/hospital-record" });
    };
    const goToConsultRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/records/consult-record" });
    };
    const goToRevisitRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/records/revisit-record" });
    };
    const goToCheckRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/records/check-record" });
    };
    const goToPrivacy = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/settings/privacy" });
    };
    const goToHelp = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/help/help" });
    };
    const goToComplain = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/settings/complain" });
    };
    const goToEvaluate = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/settings/evaluate" });
    };
    const goToUnbind = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/settings/unbind" });
    };
    const handleLogout = async () => {
      try {
        const res = await new Promise((resolve) => {
          common_vendor.index.showModal({
            title: "确认退出",
            content: "确定要退出登录吗？",
            success: (result) => resolve(result.confirm)
          });
        });
        if (res) {
          try {
            await api_user.userApi.logout();
          } catch (e) {
            common_vendor.index.__f__("log", "at pages/profile/profile.vue:196", "后端退出失败，但继续清除本地状态");
          }
          userStore.logout();
          await utils_uniHelper.uniShowToast({ title: "已退出登录" });
          await utils_uniHelper.uniSwitchTab({ url: "/pages/login/login" });
        }
      } catch (e) {
        await utils_uniHelper.uniShowToast({ title: "退出失败", icon: "none" });
      }
    };
    common_vendor.onMounted(() => {
      getUserInfo();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(userInfo.value.name || "微信用户"),
        b: common_vendor.t(userInfo.value.phone || "*************"),
        c: common_vendor.o(goToUnbind),
        d: common_assets._imports_0$1,
        e: common_vendor.o(goToMyCard),
        f: common_assets._imports_0$2,
        g: common_vendor.o(goToMyPatient),
        h: common_assets._imports_0$3,
        i: common_vendor.o(goToMyDoctor),
        j: common_assets._imports_3,
        k: common_vendor.o(goToRegisterRecord),
        l: common_assets._imports_4,
        m: common_vendor.o(goToOutpatientRecord),
        n: common_assets._imports_5,
        o: common_vendor.o(goToHospitalRecord),
        p: common_assets._imports_6,
        q: common_vendor.o(goToConsultRecord),
        r: common_assets._imports_7,
        s: common_vendor.o(goToRevisitRecord),
        t: common_assets._imports_8,
        v: common_vendor.o(goToCheckRecord),
        w: common_assets._imports_9,
        x: common_vendor.o(goToPrivacy),
        y: common_assets._imports_10,
        z: common_vendor.o(goToHelp),
        A: common_assets._imports_11,
        B: common_vendor.o(goToComplain),
        C: common_assets._imports_12,
        D: common_vendor.o(goToEvaluate),
        E: common_vendor.o(handleLogout)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-dd383ca2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
