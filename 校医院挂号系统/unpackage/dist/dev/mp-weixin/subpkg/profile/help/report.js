"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "report",
  setup(__props) {
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHome = () => {
      common_vendor.index.reLaunch({ url: "/pages/home/home" });
    };
    const goToReport = () => {
      common_vendor.index.showModal({
        title: "报告查询",
        content: "报告查询功能开发中，敬请期待",
        showCancel: false
      });
    };
    const goToCheckRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/check-record" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.o(goHome),
        c: common_vendor.o(goToReport),
        d: common_vendor.o(goToCheckRecord)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-cad1d6da"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/help/report.js.map
