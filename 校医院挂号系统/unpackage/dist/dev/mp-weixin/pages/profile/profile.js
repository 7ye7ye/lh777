"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_user = require("../../api/user.js");
const store_user = require("../../store/user.js");
const utils_uniHelper = require("../../utils/uniHelper.js");
const utils_auth = require("../../utils/auth.js");
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
    const goToMyCard = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.MY_CARD,
      "/subpkg/profile/personal/mycard",
      { requireCard: true }
    );
    const goToMyPatient = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.MY_PATIENT,
      "/subpkg/profile/personal/mypatient"
    );
    const goToMyDoctor = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.MY_DOCTOR,
      "/subpkg/profile/personal/mydoctor"
    );
    const goToRegisterRecord = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
      "/subpkg/profile/records/register-record"
    );
    const goToOutpatientRecord = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
      "/subpkg/profile/records/outpatient-record"
    );
    const goToHospitalRecord = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
      "/subpkg/profile/records/hospital-record"
    );
    const goToConsultRecord = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
      "/subpkg/profile/records/consult-record"
    );
    const goToRevisitRecord = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
      "/subpkg/profile/records/revisit-record"
    );
    const goToCheckRecord = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.RECORDS,
      "/subpkg/profile/records/check-record"
    );
    const goToPrivacy = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
      "/subpkg/profile/settings/privacy"
    );
    const goToHelp = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
      "/subpkg/profile/help/help"
    );
    const goToComplain = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
      "/subpkg/profile/settings/complain"
    );
    const goToEvaluate = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.PROFILE.SETTINGS,
      "/subpkg/profile/settings/evaluate"
    );
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
            common_vendor.index.__f__("log", "at pages/profile/profile.vue:218", "后端退出失败，但继续清除本地状态");
          }
          userStore.logout();
          await utils_uniHelper.uniShowToast({ title: "已退出登录" });
          await utils_uniHelper.uniSwitchTab({ url: "/pages/profile/profile" });
        }
      } catch (e) {
        common_vendor.index.__f__("log", "at pages/profile/profile.vue:231", e);
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
        e: common_vendor.o(handleLogout)
      }, {
        f: common_assets._imports_0$1,
        g: common_vendor.o((...args) => common_vendor.unref(goToMyCard) && common_vendor.unref(goToMyCard)(...args)),
        h: common_assets._imports_1$1,
        i: common_vendor.o((...args) => common_vendor.unref(goToMyPatient) && common_vendor.unref(goToMyPatient)(...args)),
        j: common_assets._imports_2$1,
        k: common_vendor.o((...args) => common_vendor.unref(goToMyDoctor) && common_vendor.unref(goToMyDoctor)(...args)),
        l: common_assets._imports_0$2,
        m: common_vendor.o((...args) => common_vendor.unref(goToRegisterRecord) && common_vendor.unref(goToRegisterRecord)(...args)),
        n: common_assets._imports_4,
        o: common_vendor.o((...args) => common_vendor.unref(goToOutpatientRecord) && common_vendor.unref(goToOutpatientRecord)(...args)),
        p: common_assets._imports_5,
        q: common_vendor.o((...args) => common_vendor.unref(goToHospitalRecord) && common_vendor.unref(goToHospitalRecord)(...args)),
        r: common_assets._imports_1$2,
        s: common_vendor.o((...args) => common_vendor.unref(goToConsultRecord) && common_vendor.unref(goToConsultRecord)(...args)),
        t: common_assets._imports_7,
        v: common_vendor.o((...args) => common_vendor.unref(goToRevisitRecord) && common_vendor.unref(goToRevisitRecord)(...args)),
        w: common_assets._imports_8,
        x: common_vendor.o((...args) => common_vendor.unref(goToCheckRecord) && common_vendor.unref(goToCheckRecord)(...args)),
        y: common_assets._imports_9,
        z: common_vendor.o((...args) => common_vendor.unref(goToPrivacy) && common_vendor.unref(goToPrivacy)(...args)),
        A: common_assets._imports_10,
        B: common_vendor.o((...args) => common_vendor.unref(goToHelp) && common_vendor.unref(goToHelp)(...args)),
        C: common_assets._imports_11,
        D: common_vendor.o((...args) => common_vendor.unref(goToComplain) && common_vendor.unref(goToComplain)(...args)),
        E: common_assets._imports_12,
        F: common_vendor.o((...args) => common_vendor.unref(goToEvaluate) && common_vendor.unref(goToEvaluate)(...args))
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-dd383ca2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
