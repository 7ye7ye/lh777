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
    const isLoggedIn = common_vendor.computed(() => !!userStore.isLoggedIn);
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
      common_vendor.index.navigateTo({ url: "/subpkg/profile/personal/mycard" });
    };
    const goToMyPatient = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/personal/mypatient" });
    };
    const goToMyDoctor = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/personal/mydoctor" });
    };
    const goToRegisterRecord = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/records/register-record" });
    };
    const goToOutpatientRecord = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/records/outpatient-record" });
    };
    const goToHospitalRecord = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/records/hospital-record" });
    };
    const goToConsultRecord = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/records/consult-record" });
    };
    const goToRevisitRecord = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/records/revisit-record" });
    };
    const goToCheckRecord = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/records/check-record" });
    };
    const goToPrivacy = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/settings/privacy" });
    };
    const goToHelp = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/help/help" });
    };
    const goToComplain = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/settings/complain" });
    };
    const goToEvaluate = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/settings/evaluate" });
    };
    const goToUnbind = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/settings/unbind" });
    };
    const goLogin = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/auth/login" });
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
            common_vendor.index.__f__("log", "at pages/profile/profile.vue:207", "后端退出失败，但继续清除本地状态");
          }
          userStore.logout();
          await utils_uniHelper.uniShowToast({ title: "已退出登录" });
          await utils_uniHelper.uniNavigateTo({ url: "/subpkg/auth/login" });
        }
      } catch (e) {
        await utils_uniHelper.uniShowToast({ title: "退出失败", icon: "none" });
      }
    };
    common_vendor.onMounted(() => {
      getUserInfo();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(userInfo.value.name || "微信用户"),
        b: common_vendor.t(userInfo.value.phone || "*************"),
        c: !isLoggedIn.value
      }, !isLoggedIn.value ? {
        d: common_vendor.o(goLogin)
      } : {
        e: common_vendor.o(goToUnbind)
      }, {
        f: common_assets._imports_0$2,
        g: common_vendor.o(goToMyCard),
        h: common_assets._imports_1$1,
        i: common_vendor.o(goToMyPatient),
        j: common_assets._imports_2$1,
        k: common_vendor.o(goToMyDoctor),
        l: common_assets._imports_3,
        m: common_vendor.o(goToRegisterRecord),
        n: common_assets._imports_4,
        o: common_vendor.o(goToOutpatientRecord),
        p: common_assets._imports_5,
        q: common_vendor.o(goToHospitalRecord),
        r: common_assets._imports_6,
        s: common_vendor.o(goToConsultRecord),
        t: common_assets._imports_7,
        v: common_vendor.o(goToRevisitRecord),
        w: common_assets._imports_8,
        x: common_vendor.o(goToCheckRecord),
        y: common_assets._imports_9,
        z: common_vendor.o(goToPrivacy),
        A: common_assets._imports_10,
        B: common_vendor.o(goToHelp),
        C: common_assets._imports_11,
        D: common_vendor.o(goToComplain),
        E: common_assets._imports_12,
        F: common_vendor.o(goToEvaluate),
        G: isLoggedIn.value
      }, isLoggedIn.value ? {
        H: common_vendor.o(handleLogout)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-dd383ca2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
