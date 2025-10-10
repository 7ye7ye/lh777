"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "help",
  setup(__props) {
    const searchKeyword = common_vendor.ref("");
    const commonIssues = common_vendor.ref([
      { id: 1, title: "登录注册", category: "account" },
      { id: 2, title: "绑卡", category: "card" },
      { id: 3, title: "挂号问题", category: "registration" },
      { id: 4, title: "门诊缴费", category: "payment" },
      { id: 5, title: "住院", category: "hospitalization" },
      { id: 6, title: "报告查询", category: "report" },
      { id: 7, title: "其他", category: "other" },
      { id: 8, title: "预约挂号", category: "appointment" }
    ]);
    const filteredIssues = common_vendor.computed(() => {
      if (!searchKeyword.value.trim()) {
        return commonIssues.value;
      }
      const keyword = searchKeyword.value.toLowerCase();
      return commonIssues.value.filter(
        (issue) => issue.title.toLowerCase().includes(keyword)
      );
    });
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHome = () => {
      common_vendor.index.reLaunch({ url: "/pages/home/home" });
    };
    const onSearch = () => {
    };
    const performSearch = () => {
      if (!searchKeyword.value.trim()) {
        common_vendor.index.showToast({ title: "请输入搜索关键字", icon: "none" });
        return;
      }
      common_vendor.index.showToast({ title: `搜索"${searchKeyword.value}"`, icon: "none" });
    };
    const goToIssueDetail = (issueId) => {
      const issue = commonIssues.value.find((item) => item.id === issueId);
      const routeMap = {
        1: "/pages/profile/help/login",
        // 登录注册
        2: "/pages/profile/help/bindcard",
        // 绑卡
        3: "/pages/profile/help/register",
        // 挂号问题
        4: "/pages/profile/help/payment",
        // 门诊缴费
        5: "/pages/profile/help/hospitalization",
        // 住院
        6: "/pages/profile/help/report",
        // 报告查询
        7: "/pages/profile/help/other",
        // 其他
        8: "/pages/profile/help/appointment"
        // 预约挂号
      };
      const route = routeMap[issueId];
      if (route) {
        common_vendor.index.navigateTo({ url: route });
      } else {
        common_vendor.index.showModal({
          title: issue.title,
          content: "这是关于" + issue.title + "的详细解答...",
          showCancel: false
        });
      }
    };
    const goToFeedback = () => {
      common_vendor.index.navigateTo({ url: "/pages/profile/settings/complain" });
    };
    common_vendor.onMounted(() => {
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(goBack),
        b: common_vendor.o(goHome),
        c: common_vendor.o([($event) => searchKeyword.value = $event.detail.value, onSearch]),
        d: searchKeyword.value,
        e: common_vendor.o(performSearch),
        f: common_vendor.f(filteredIssues.value, (issue, k0, i0) => {
          return {
            a: common_vendor.t(issue.title),
            b: issue.id,
            c: common_vendor.o(($event) => goToIssueDetail(issue.id), issue.id)
          };
        }),
        g: common_vendor.o(goToFeedback)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-776f6bed"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/profile/help/help.js.map
