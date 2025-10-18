"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "register-record",
  setup(__props) {
    const records = common_vendor.ref([
      { id: 1, dept: "内科", doctor: "李医生", time: "2025-09-21 09:00" }
    ]);
    const getRegisterRecord = () => {
      api_user.userApi.getRegisterRecord().then((res) => {
        common_vendor.index.showToast({ title: "获取成功", icon: "success" });
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(records.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.dept),
            b: common_vendor.t(item.doctor),
            c: common_vendor.t(item.time),
            d: item.id
          };
        }),
        b: common_vendor.o(getRegisterRecord)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-13db1a12"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/records/register-record.js.map
