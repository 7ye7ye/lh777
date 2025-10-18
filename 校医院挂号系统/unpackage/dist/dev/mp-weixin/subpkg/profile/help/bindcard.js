"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "bindcard",
  setup(__props) {
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHome = () => {
      common_vendor.index.reLaunch({ url: "/pages/home/home" });
    };
    const goToBindCard = () => {
      common_vendor.index.showModal({
        title: "绑卡功能",
        content: "绑卡功能开发中，敬请期待",
        showCancel: false
      });
    };
    const goToMyCard = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/mycard" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.o(goHome),
        c: common_vendor.o(goToBindCard),
        d: common_vendor.o(goToMyCard)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ac20c051"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/help/bindcard.js.map
