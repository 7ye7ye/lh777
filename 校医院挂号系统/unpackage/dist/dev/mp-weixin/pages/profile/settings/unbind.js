"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "unbind",
  setup(__props) {
    const userInfo = common_vendor.ref({});
    const unbindReason = common_vendor.ref("");
    const showConfirmModal = common_vendor.ref(false);
    const getUserInfo = () => {
      api_user.userApi.getCurrentUser().then((res) => {
        userInfo.value = res.data || {};
      }).catch(() => {
        userInfo.value = {
          name: "微信用户",
          phone: "15******068",
          bindTime: "2025-01-01"
        };
      });
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const confirmUnbind = () => {
      showConfirmModal.value = true;
    };
    const executeUnbind = () => {
      const unbindData = {
        reason: unbindReason.value
      };
      api_user.userApi.unbindAccount(unbindData).then(() => {
        common_vendor.index.showToast({ title: "解绑成功", icon: "success" });
        showConfirmModal.value = false;
        setTimeout(() => {
          common_vendor.index.reLaunch({ url: "/pages/login/login" });
        }, 1500);
      }).catch(() => {
        common_vendor.index.showToast({ title: "解绑失败，请重试", icon: "error" });
        showConfirmModal.value = false;
      });
    };
    common_vendor.onMounted(() => {
      getUserInfo();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(userInfo.value.name || "微信用户"),
        b: common_vendor.t(userInfo.value.phone || "15******068"),
        c: common_vendor.t(userInfo.value.bindTime || "2025-01-01"),
        d: unbindReason.value,
        e: common_vendor.o(($event) => unbindReason.value = $event.detail.value),
        f: common_vendor.t(unbindReason.value.length),
        g: common_vendor.o(goBack),
        h: common_vendor.o(confirmUnbind),
        i: showConfirmModal.value
      }, showConfirmModal.value ? {
        j: common_vendor.o(($event) => showConfirmModal.value = false),
        k: common_vendor.o(executeUnbind)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f05135a0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/profile/settings/unbind.js.map
