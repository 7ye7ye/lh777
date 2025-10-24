"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const utils_auth = require("../../utils/auth.js");
if (!Math) {
  LoginPrompt();
}
const LoginPrompt = () => "../../components/LoginPrompt.js";
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const tabs = ["门诊", "住院", "体检", "其他"];
    const activeIndex = common_vendor.ref(0);
    const loginPromptRef = common_vendor.ref(null);
    const itemsMap = {
      门诊: [
        { icon: "🌙", text: "晚间门诊" },
        { icon: "📅", text: "周末门诊" },
        { icon: "📋", text: "门诊签到" },
        { icon: "🧠", text: "心理筛查门诊" },
        { icon: "🗓️", text: "超声签到" },
        { icon: "🧾", text: "看结果K号" },
        { icon: "💴", text: "门诊缴费" },
        { icon: "🔎", text: "检查预约" },
        { icon: "🧾", text: "电子发票" },
        { icon: "📂", text: "电子票夹" },
        { icon: "🧭", text: "院内导航" },
        { icon: "📘", text: "门诊服务指南" },
        { icon: "📝", text: "预约记录" },
        { icon: "💬", text: "护理咨询" },
        { icon: "💳", text: "就诊卡余额退款" },
        { icon: "📚", text: "病史采集" },
        { icon: "🤖", text: "智能导诊" }
      ],
      住院: [
        { icon: "💳", text: "住院预交" },
        { icon: "🧾", text: "在院费用查询" },
        { icon: "🪪", text: "电子陪护证" },
        { icon: "📄", text: "病案复印" },
        { icon: "🧾", text: "住院发票清单" },
        { icon: "📘", text: "住院服务指南" },
        { icon: "🍱", text: "住院订餐" },
        { icon: "🧾", text: "订单清单" },
        { icon: "🍼", text: "出生证预约" },
        { icon: "🧠", text: "心理筛查住院" },
        { icon: "📊", text: "满意度调查" }
      ],
      体检: [
        { icon: "👤", text: "个检预约" },
        { icon: "👥", text: "团检预约" },
        { icon: "🗂️", text: "体检报告" },
        { icon: "🧾", text: "体检订单" },
        { icon: "🏥", text: "体检中心" }
      ],
      其他: [
        { icon: "📚", text: "健康百科" },
        { icon: "📣", text: "科普宣教" },
        { icon: "🆘", text: "帮助与反馈" },
        { icon: "💴", text: "价目公示" },
        { icon: "➕", text: "移动随访" },
        { icon: "🚑", text: "院前急救" },
        { icon: "💉", text: "惠民复诊" }
      ]
    };
    const currentItems = common_vendor.computed(() => itemsMap[tabs[activeIndex.value]] || []);
    const onItemClick = (item) => {
      common_vendor.index.showToast({ title: item.text, icon: "none" });
    };
    const onVisitCardClick = utils_auth.createAuthHandler(
      utils_auth.AUTH_REQUIRED_FEATURES.HOME.VISIT_CARD,
      "/subpkg/profile/personal/mycard",
      { requireCard: true }
    );
    return (_ctx, _cache) => {
      return {
        a: common_assets._imports_0,
        b: common_vendor.o((...args) => common_vendor.unref(onVisitCardClick) && common_vendor.unref(onVisitCardClick)(...args)),
        c: common_vendor.f(tabs, (tab, idx, i0) => {
          return {
            a: common_vendor.t(tab),
            b: tab,
            c: idx === activeIndex.value ? 1 : "",
            d: common_vendor.o(($event) => activeIndex.value = idx, tab)
          };
        }),
        d: common_vendor.f(currentItems.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.icon),
            b: common_vendor.t(item.text),
            c: item.text,
            d: common_vendor.o(($event) => onItemClick(item), item.text)
          };
        }),
        e: common_vendor.sr(loginPromptRef, "07e72d3c-0", {
          "k": "loginPromptRef"
        }),
        f: common_vendor.p({
          mode: "inline",
          message: "登录后可出示电子就诊码",
          ["login-text"]: "去登录"
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-07e72d3c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/home/home.js.map
