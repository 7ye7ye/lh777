"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "payment",
  setup(__props) {
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHome = () => {
      common_vendor.index.reLaunch({ url: "/pages/home/home" });
    };
    const goToPayment = () => {
      common_vendor.index.showModal({
        title: "缴费功能",
        content: "缴费功能开发中，敬请期待",
        showCancel: false
      });
    };
    const goToRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/outpatient-record" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.o(goHome),
        c: common_vendor.o(goToPayment),
        d: common_vendor.o(goToRecord)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-00af828a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/help/payment.js.map
