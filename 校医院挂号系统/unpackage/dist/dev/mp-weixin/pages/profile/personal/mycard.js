"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "mycard",
  setup(__props) {
    const cardInfo = common_vendor.ref({});
    const recentRecords = common_vendor.ref([
      { id: 1, description: "门诊挂号费", time: "2025-09-20 14:30", amount: "15.00", type: "expense" },
      { id: 2, description: "药品费用", time: "2025-09-19 10:15", amount: "128.50", type: "expense" },
      { id: 3, description: "卡充值", time: "2025-09-18 16:20", amount: "200.00", type: "income" }
    ]);
    const getCardInfo = () => {
      api_user.userApi.getCard().then((res) => {
        common_vendor.index.showToast({ title: "获取成功", icon: "success" });
        cardInfo.value = res.data || {};
      }).catch(() => {
        common_vendor.index.showToast({ title: "获取失败", icon: "error" });
      });
    };
    const goToRecharge = () => {
      common_vendor.index.showModal({
        title: "充值功能",
        content: "充值功能开发中，敬请期待",
        showCancel: false
      });
    };
    const goToHistory = () => {
      common_vendor.index.showModal({
        title: "消费记录",
        content: "消费记录功能开发中，敬请期待",
        showCancel: false
      });
    };
    common_vendor.onMounted(() => {
      getCardInfo();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(cardInfo.value.name || "张三"),
        b: common_vendor.t(cardInfo.value.cardNumber || "1234567890"),
        c: common_vendor.t(cardInfo.value.balance || "0.00"),
        d: common_vendor.t(cardInfo.value.status || "正常"),
        e: common_vendor.n(cardInfo.value.status === "正常" ? "active" : "inactive"),
        f: common_vendor.o(getCardInfo),
        g: common_vendor.o(goToRecharge),
        h: common_vendor.o(goToHistory),
        i: common_vendor.f(recentRecords.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.description),
            b: common_vendor.t(item.time),
            c: common_vendor.t(item.type === "income" ? "+" : "-"),
            d: common_vendor.t(item.amount),
            e: common_vendor.n(item.type === "income" ? "income" : "expense"),
            f: item.id
          };
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1d27c906"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/profile/personal/mycard.js.map
