"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  data() {
    return {
      appointmentId: null,
      // 从上个页面传来的预约ID
      messageDetailList: []
      // 存储消息详情列表
    };
  },
  // uni-app生命周期函数，在页面加载时执行，可以获取路由参数
  onLoad(options) {
    if (options.appointmentId) {
      this.appointmentId = options.appointmentId;
      this.fetchMessageDetail();
    } else {
      common_vendor.index.showToast({ title: "加载失败，缺少预约ID", icon: "none" });
    }
  },
  methods: {
    fetchMessageDetail() {
      const apiUrl = `http://10.61.62.249:8095/jeecg-boot/api/messages/detail`;
      common_vendor.index.request({
        url: `${apiUrl}?appointmentId=${this.appointmentId}`,
        method: "GET",
        success: (res) => {
          if (res.statusCode === 200) {
            this.messageDetailList = res.data.map((item) => {
              if (typeof item.content === "string") {
                try {
                  item.content = JSON.parse(item.content);
                } catch (e) {
                  item.content = {};
                }
              }
              return item;
            });
          } else {
            common_vendor.index.showToast({ title: "加载详情失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at subpkg/messages/detail.vue:87", "API请求失败:", err);
          common_vendor.index.showToast({ title: "网络请求失败", icon: "none" });
        }
      });
    },
    // 跳转到最终的挂号回执单页面
    goToReceipt(appointmentId) {
      common_vendor.index.navigateTo({
        url: `/subpkg/messages/receipt?id=${appointmentId}`
      });
    },
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr)
        return "";
      return dateTimeStr.replace("T", " ");
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.f($data.messageDetailList, (item, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t($options.formatDateTime(item.createdTime)),
        b: common_vendor.t(item.title),
        c: item.content.patient_card_no
      }, item.content.patient_card_no ? {
        d: common_vendor.t(item.content.patient_card_no)
      } : {}, {
        e: item.content.patient_name
      }, item.content.patient_name ? {
        f: common_vendor.t(item.content.patient_name)
      } : {}, {
        g: item.content.doctor_name
      }, item.content.doctor_name ? {
        h: common_vendor.t(item.content.doctor_name)
      } : {}, {
        i: item.content.department_name
      }, item.content.department_name ? {
        j: common_vendor.t(item.content.department_name)
      } : {}, {
        k: item.content.appointment_time
      }, item.content.appointment_time ? {
        l: common_vendor.t(item.content.appointment_time)
      } : {}, {
        m: item.content.hospital_remark
      }, item.content.hospital_remark ? {
        n: common_vendor.t(item.content.hospital_remark)
      } : {}, {
        o: common_vendor.o(($event) => $options.goToReceipt(item.appointmentId), item.messageId),
        p: item.messageId
      });
    }),
    b: common_assets._imports_0$4
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-226550a4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subpkg/messages/detail.js.map
