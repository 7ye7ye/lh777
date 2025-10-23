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
        { icon: "👤", text: "个检预约", url: "/pages/physical-exam/physical-exam" },
        { icon: "👥", text: "团检预约", url: "/subpkg/physical-exam/group-booking" },
        { icon: "🗂️", text: "体检报告", url: "/subpkg/physical-exam/exam-report" },
        { icon: "🧾", text: "体检订单", url: "/subpkg/physical-exam/exam-orders" },
        { icon: "🏥", text: "体检中心", url: "/subpkg/physical-exam/exam-center" }
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
    const onQuickItemClick = (type) => {
      common_vendor.index.__f__("log", "at pages/home/home.vue:143", "点击快捷入口:", type);
      switch (type) {
        case "disease":
          common_vendor.index.__f__("log", "at pages/home/home.vue:148", "准备跳转到疾病指南页面");
          common_vendor.index.navigateTo({
            url: "/subpkg/hospital/disease-guide",
            success: () => {
              common_vendor.index.__f__("log", "at pages/home/home.vue:152", "疾病指南页面跳转成功");
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/home/home.vue:155", "疾病指南页面跳转失败:", err);
              common_vendor.index.showToast({
                title: "页面跳转失败: " + (err.errMsg || "未知错误"),
                icon: "none",
                duration: 3e3
              });
            }
          });
          break;
        case "department":
          common_vendor.index.__f__("log", "at pages/home/home.vue:166", "准备跳转到科室挂号页面");
          common_vendor.index.navigateTo({
            url: "/subpkg/hospital/department-booking",
            success: () => {
              common_vendor.index.__f__("log", "at pages/home/home.vue:170", "科室挂号页面跳转成功");
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/home/home.vue:173", "科室挂号页面跳转失败:", err);
              common_vendor.index.showToast({
                title: "页面跳转失败: " + (err.errMsg || "未知错误"),
                icon: "none",
                duration: 3e3
              });
            }
          });
          break;
        case "report":
          common_vendor.index.showToast({ title: "报告查询功能开发中", icon: "none" });
          break;
        case "internet":
          common_vendor.index.showToast({ title: "互联网诊疗功能开发中", icon: "none" });
          break;
      }
    };
    const consultContent = {
      before: {
        title: "体检前注意事项",
        content: `1. 体检前一天
• 晚餐清淡，避免油腻食物
• 晚上8点后禁食
• 不要饮酒，保证充足睡眠

2. 体检当天
• 空腹（禁食8-12小时）
• 可少量饮水
• 穿宽松衣服
• 携带有效证件

3. 女性注意
• 避开生理期
• 怀孕或备孕请提前告知

4. 慢性病患者
• 高血压、糖尿病患者可少量饮水服药
• 携带近期病历和处方`
      },
      report: {
        title: "体检报告解读",
        content: `1. 体检报告领取
• 一般3-5个工作日
• 可在线查看或现场领取

2. 报告内容
• 各项检查结果
• 异常指标标注
• 医生总结和建议

3. 异常指标处理
• 轻度异常：注意复查
• 中度异常：门诊咨询
• 重度异常：及时就医

4. 免费服务
• 报告解读咨询
• 健康管理建议
• 异常指标复查指导`
      },
      package: {
        title: "如何选择体检套餐",
        content: `1. 基础套餐（280元）
适合：学生、青年教职工
包含：15项常规检查

2. 教职工套餐（480元）★推荐
适合：在职教职工
特色：学校报销、职业病筛查
包含：25项全面检查

3. 全面套餐（880元）
适合：50岁以上、有基础疾病
特色：深度筛查、跟踪服务
包含：35项全面检查

提示：
• 学生体检免费
• 教职工基础套餐学校报销
• 老年人基础套餐免费`
      },
      booking: {
        title: "预约流程",
        content: `1. 在线预约（推荐）
• 打开校医院挂号系统小程序
• 选择体检科→选择体检类型
• 选择日期和时间段
• 填写个人信息并确认

2. 电话预约
• 拨打：010-51682525转体检科
• 提供身份信息
• 选择体检日期

3. 现场预约
• 前往体检中心1楼服务台
• 出示有效证件
• 填写预约表

4. 集体预约
• 新生：随录取通知书说明
• 学生：学生处统一安排
• 教职工：人事处统一组织`
      },
      time: {
        title: "体检时间安排",
        content: `1. 常规体检时间
• 周一至周五 7:30-11:00
• 周六 8:00-11:00（预约）
• 采血时间：7:30-10:00

2. 特殊时间安排
• 新生入学体检：8月25日-9月5日
  每日7:00-17:00
• 学生年度体检：9-11月
  集体：周一至周五 7:00-11:00
  补检：周一、三、五 13:00-16:00
• 教职工体检：3-6月
  周一至周五 7:30-11:00

3. 建议
• 尽量选择工作日早晨
• 避免月初、周一高峰
• 提前预约可节省时间`
      },
      price: {
        title: "收费政策",
        content: `1. 免费项目
• 学生常规体检（学校承担）
• 新生入学体检（学校承担）
• 教职工基础套餐（学校报销）
• 老年人基础套餐（国家项目）

2. 收费项目
• 基础套餐：280元
• 教职工套餐：480元（报销后0元）
• 全面套餐：880元
• 升级项目：按项目收费

3. 优惠政策
• 教职工家属：9折优惠
• 校友：9.5折优惠
• 团体预约（10人以上）：9折

4. 支付方式
• 微信/支付宝
• 校园一卡通
• 医保卡（部分项目）`
      }
    };
    const showConsultDialog = (question) => {
      const content = consultContent[question];
      if (content) {
        common_vendor.index.showModal({
          title: content.title,
          content: content.content,
          showCancel: true,
          cancelText: "关闭",
          confirmText: "电话咨询",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.makePhoneCall({
                phoneNumber: "010-51682525"
              });
            }
          }
        });
      }
    };
    const onItemClick = (item) => {
      if (item.type === "consult") {
        showConsultDialog(item.question);
        return;
      }
      if (item.url) {
        common_vendor.index.navigateTo({
          url: item.url,
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/home/home.vue:256", "页面跳转失败:", err);
            common_vendor.index.showToast({ title: "页面跳转失败", icon: "none" });
          }
        });
      } else {
        common_vendor.index.showToast({ title: `${item.text}功能开发中`, icon: "none" });
      }
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
        c: common_vendor.o(($event) => onQuickItemClick("disease")),
        d: common_vendor.o(($event) => onQuickItemClick("department")),
        e: common_vendor.o(($event) => onQuickItemClick("report")),
        f: common_vendor.o(($event) => onQuickItemClick("internet")),
        g: common_vendor.f(tabs, (tab, idx, i0) => {
          return {
            a: common_vendor.t(tab),
            b: tab,
            c: idx === activeIndex.value ? 1 : "",
            d: common_vendor.o(($event) => activeIndex.value = idx, tab)
          };
        }),
        h: common_vendor.f(currentItems.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.icon),
            b: common_vendor.t(item.text),
            c: item.text,
            d: common_vendor.o(($event) => onItemClick(item), item.text)
          };
        }),
        i: common_vendor.sr(loginPromptRef, "07e72d3c-0", {
          "k": "loginPromptRef"
        }),
        j: common_vendor.p({
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
