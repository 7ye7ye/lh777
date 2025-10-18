"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "hospital-record",
  setup(__props) {
    const records = common_vendor.ref([
      { id: 1, hospitalNo: "0001234567", amount: "1000元", time: "2025-09-21 09:00" }
    ]);
    const getHospitalRecord = () => {
      api_user.userApi.getHospitalRecord().then((res) => {
        common_vendor.index.showToast({ title: "获取成功", icon: "success" });
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(records.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.hospitalNo),
            b: common_vendor.t(item.amount),
            c: common_vendor.t(item.time),
            d: item.id
          };
        }),
        b: common_vendor.o(getHospitalRecord)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ffc79cc5"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/records/hospital-record.js.map
