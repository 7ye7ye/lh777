"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "outpatient-record",
  setup(__props) {
    const records = common_vendor.ref([
      {
        id: 1,
        project: "挂号费",
        amount: "15.00",
        time: "2025-09-21 09:00",
        dept: "内科",
        status: "已支付"
      },
      {
        id: 2,
        project: "药品费用",
        amount: "128.50",
        time: "2025-09-20 14:30",
        dept: "药房",
        status: "已支付"
      },
      {
        id: 3,
        project: "检查费",
        amount: "200.00",
        time: "2025-09-19 10:15",
        dept: "检验科",
        status: "未支付"
      }
    ]);
    const monthlyTotal = common_vendor.computed(() => {
      const currentMonth = (/* @__PURE__ */ new Date()).getMonth();
      const currentYear = (/* @__PURE__ */ new Date()).getFullYear();
      return records.value.filter((record) => {
        const recordDate = new Date(record.time);
        return recordDate.getMonth() === currentMonth && recordDate.getFullYear() === currentYear && record.status === "已支付";
      }).reduce((total, record) => total + parseFloat(record.amount), 0).toFixed(2);
    });
    const totalCount = common_vendor.computed(() => {
      return records.value.filter((record) => record.status === "已支付").length;
    });
    const getOutpatientRecord = () => {
      api_user.userApi.getOutpatientRecord().then((res) => {
        common_vendor.index.showToast({ title: "获取成功", icon: "success" });
      }).catch(() => {
        common_vendor.index.showToast({ title: "获取失败", icon: "error" });
      });
    };
    const payRecord = (id) => {
      common_vendor.index.showModal({
        title: "确认支付",
        content: "确定要支付这笔费用吗？",
        success: (res) => {
          if (res.confirm) {
            const record = records.value.find((r) => r.id === id);
            if (record) {
              record.status = "已支付";
              common_vendor.index.showToast({ title: "支付成功", icon: "success" });
            }
          }
        }
      });
    };
    common_vendor.onMounted(() => {
      getOutpatientRecord();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(monthlyTotal.value),
        b: common_vendor.t(totalCount.value),
        c: common_vendor.f(records.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.time),
            b: common_vendor.t(item.status),
            c: common_vendor.n(item.status === "已支付" ? "paid" : "unpaid"),
            d: common_vendor.t(item.project),
            e: common_vendor.t(item.dept),
            f: common_vendor.t(item.amount),
            g: item.status === "未支付"
          }, item.status === "未支付" ? {
            h: common_vendor.o(($event) => payRecord(item.id), item.id)
          } : {}, {
            i: item.id
          });
        }),
        d: records.value.length === 0
      }, records.value.length === 0 ? {} : {}, {
        e: common_vendor.o(getOutpatientRecord)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-87fcb737"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/records/outpatient-record.js.map
