"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "other",
  setup(__props) {
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHome = () => {
      common_vendor.index.reLaunch({ url: "/pages/home/home" });
    };
    const goToFeedback = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/complain" });
    };
    const goToContact = () => {
      common_vendor.index.showModal({
        title: "联系我们",
        content: "客服电话：010-51688120\n服务时间：工作日 8:00-17:00\n邮箱：hospital@bjtu.edu.cn",
        showCancel: false
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.o(goHome),
        c: common_vendor.o(goToFeedback),
        d: common_vendor.o(goToContact)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-725e3deb"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/profile/help/other.js.map
