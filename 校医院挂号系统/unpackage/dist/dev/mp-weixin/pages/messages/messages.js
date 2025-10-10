"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "messages",
  setup(__props) {
    const goLogin = () => {
      common_vendor.index.navigateTo({ url: "/pages/login/login" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goLogin)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ecc172b4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/messages/messages.js.map
