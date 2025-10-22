"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "messages",
  setup(__props) {
    const messages = common_vendor.ref([]);
    common_vendor.onMounted(() => {
      messages.value = [
        { id: 1, title: "挂号成功通知", time: "2025-10-17 09:00", content: "请及时前往诊室" },
        { id: 2, title: "排班变更提醒", time: "2025-10-18 12:00", content: "您有新的排班变更" }
      ];
    });
    const goDoctorMain = () => {
      common_vendor.index.navigateTo({ url: "/pages/doctor/schedule/main" });
    };
    const openDetail = (item) => {
      common_vendor.index.navigateTo({ url: `/subpkg/messages/detail?id=${item.id}` });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goDoctorMain),
        b: common_vendor.f(messages.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.title),
            b: common_vendor.t(item.time),
            c: common_vendor.t(item.content),
            d: item.id,
            e: common_vendor.o(($event) => openDetail(item), item.id)
          };
        }),
        c: messages.value.length === 0
      }, messages.value.length === 0 ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ecc172b4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/messages/messages.js.map
