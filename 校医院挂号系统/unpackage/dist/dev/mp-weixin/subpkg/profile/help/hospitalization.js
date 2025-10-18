"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "hospitalization",
  setup(__props) {
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHome = () => {
      common_vendor.index.reLaunch({ url: "/pages/home/home" });
    };
    const goToHospitalization = () => {
      common_vendor.index.showModal({
        title: "住院登记",
        content: "住院登记功能开发中，敬请期待",
        showCancel: false
      });
    };
    const goToRecord = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/hospital-record" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.o(goHome),
        c: common_vendor.o(goToHospitalization),
        d: common_vendor.o(goToRecord)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a78482ef"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/help/hospitalization.js.map
