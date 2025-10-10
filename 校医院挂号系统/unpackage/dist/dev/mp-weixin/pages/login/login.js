"use strict";
const common_vendor = require("../../common/vendor.js");
const store_user = require("../../store/user.js");
const api_user = require("../../api/user.js");
const utils_uniHelper = require("../../utils/uniHelper.js");
const _sfc_main = {
  __name: "login",
  setup(__props) {
    const form = common_vendor.ref({
      userAccount: "",
      userPassword: ""
    });
    const loading = common_vendor.ref(false);
    const disabled = common_vendor.computed(() => !form.value.userAccount || !form.value.userPassword || loading.value);
    const userStore = store_user.useUserStore();
    const onSubmit = async () => {
      if (disabled.value)
        return;
      loading.value = true;
      try {
        const res = await api_user.userApi.login({ userAccount: form.value.userAccount, userPassword: form.value.userPassword });
        const token = (res == null ? void 0 : res.token) || "";
        const userInfo = (res == null ? void 0 : res.user) || null;
        if (token) {
          userStore.setToken(token);
          if (userInfo) {
            userStore.setUserInfo(userInfo);
          }
          await utils_uniHelper.uniShowToast({ title: "登录成功" });
          await utils_uniHelper.uniSwitchTab({ url: "/pages/home/home" });
        } else {
          await utils_uniHelper.uniShowToast({ title: "登录失败：未获取到token", icon: "none" });
        }
      } catch (e) {
        await utils_uniHelper.uniShowToast({ title: e && e.message || "登录失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const goRegister = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/register/register" });
    };
    const goDoctorHome = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/doctor/schedule/schedule" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(onSubmit),
        b: form.value.userAccount,
        c: common_vendor.o(common_vendor.m(($event) => form.value.userAccount = $event.detail.value, {
          trim: true
        })),
        d: common_vendor.o(onSubmit),
        e: form.value.userPassword,
        f: common_vendor.o(common_vendor.m(($event) => form.value.userPassword = $event.detail.value, {
          trim: true
        })),
        g: loading.value,
        h: disabled.value,
        i: common_vendor.o(onSubmit),
        j: common_vendor.o(goRegister),
        k: common_vendor.o(goDoctorHome)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-e4e4508d"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/login.js.map
