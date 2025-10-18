"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "consult-record",
  setup(__props) {
    const records = common_vendor.ref([
      { id: 1, content: "如何挂号？", time: "2025-09-21 09:00" }
    ]);
    const getConsultRecord = () => {
      api_user.userApi.getConsultRecord().then((res) => {
        common_vendor.index.showToast({ title: "获取成功", icon: "success" });
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(records.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.content),
            b: common_vendor.t(item.time),
            c: item.id
          };
        }),
        b: common_vendor.o(getConsultRecord)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-760d89df"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/records/consult-record.js.map
