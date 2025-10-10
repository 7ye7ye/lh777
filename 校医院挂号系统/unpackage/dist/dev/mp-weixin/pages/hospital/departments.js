"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "departments",
  setup(__props) {
    const departments = common_vendor.ref([
      { key: "neike", name: "内科", date: "2014-05-09" },
      { key: "waike", name: "外科", date: "2014-05-09" },
      { key: "zhongyi", name: "中医理疗科", date: "2014-05-09" },
      { key: "kouqiang", name: "口腔科", date: "2024-12-11" },
      { key: "huli", name: "护理部", date: "2014-05-09" },
      { key: "jiankang", name: "健康管理中心", date: "2014-05-09" },
      { key: "yiji", name: "保健医技科", date: "2024-12-11" },
      { key: "yaoji", name: "药剂科", date: "2014-05-09" },
      { key: "jingshen", name: "精神卫生防治科", date: "2024-12-11" },
      { key: "dongqu", name: "东校区门诊部", date: "2024-12-11" },
      { key: "caiwu", name: "院办财务科", date: "2024-12-11" }
    ]);
    const goDetail = (key) => {
      common_vendor.index.navigateTo({ url: `/pages/hospital/department-detail?key=${key}` });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(departments.value, (dept, k0, i0) => {
          return {
            a: common_vendor.t(dept.name),
            b: common_vendor.t(dept.date),
            c: dept.key,
            d: common_vendor.o(($event) => goDetail(dept.key), dept.key)
          };
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-f7d7c2e2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/hospital/departments.js.map
