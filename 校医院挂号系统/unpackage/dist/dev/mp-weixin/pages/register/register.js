"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
const utils_uniHelper = require("../../utils/uniHelper.js");
const _sfc_main = {
  __name: "register",
  setup(__props) {
    const form = common_vendor.ref({
      userAccount: "",
      userPassword: "",
      checkPassword: ""
    });
    const loading = common_vendor.ref(false);
    const disabled = common_vendor.computed(() => !form.value.userAccount || !form.value.userPassword || !form.value.checkPassword || loading.value);
    const onSubmit = async () => {
      if (disabled.value)
        return;
      loading.value = true;
      try {
        if (form.value.userPassword !== form.value.checkPassword) {
          await utils_uniHelper.uniShowToast({ title: "两次密码不一致", icon: "none" });
          return;
        }
        const res = await api_user.userApi.register({ userAccount: form.value.userAccount, userPassword: form.value.userPassword, checkPassword: form.value.checkPassword });
        common_vendor.index.__f__("log", "at pages/register/register.vue:85", res);
        await utils_uniHelper.uniShowToast({ title: "注册成功" });
        await utils_uniHelper.uniNavigateTo({ url: "/pages/login/login" });
      } catch (e) {
        await utils_uniHelper.uniShowToast({ title: e && e.message || "注册失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    const goLogin = () => {
      utils_uniHelper.uniNavigateTo({ url: "/pages/login/login" });
    };
    return (_ctx, _cache) => {
      return {
        a: form.value.userAccount,
        b: common_vendor.o(common_vendor.m(($event) => form.value.userAccount = $event.detail.value, {
          trim: true
        })),
        c: form.value.userPassword,
        d: common_vendor.o(common_vendor.m(($event) => form.value.userPassword = $event.detail.value, {
          trim: true
        })),
        e: form.value.checkPassword,
        f: common_vendor.o(common_vendor.m(($event) => form.value.checkPassword = $event.detail.value, {
          trim: true
        })),
        g: loading.value,
        h: disabled.value,
        i: common_vendor.o(onSubmit),
        j: common_vendor.o(goLogin)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-bac4a35d"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/register/register.js.map
