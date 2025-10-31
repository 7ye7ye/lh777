"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  data() {
    return {
      messageList: [],
      // 存储从后端获取的原始消息列表
      loading: false
      // 加载状态，防止重复请求
    };
  },
  computed: {
    // 计算属性，将原始列表处理成按 appointmentId 分组的结构
    groupedMessages() {
      if (this.messageList.length === 0) {
        return [];
      }
      const groups = {};
      this.messageList.forEach((msg) => {
        if (!groups[msg.appointmentId]) {
          groups[msg.appointmentId] = {
            appointmentId: msg.appointmentId,
            latestMessage: msg
            // 默认第一条就是最新的
          };
        }
      });
      return Object.values(groups);
    }
  },
  // uni-app生命周期函数，每次进入页面都会触发
  onShow() {
    this.fetchMessageList();
  },
  // uni-app生命周期函数，监听下拉刷新
  onPullDownRefresh() {
    this.fetchMessageList();
  },
  methods: {
    // 从后端接口获取消息列表
    fetchMessageList() {
      if (this.loading)
        return;
      this.loading = true;
      const apiUrl = "http://172.20.10.2:8095/jeecg-boot/api/messages/list";
      const testUserId = "wuzhizhu_001";
      common_vendor.index.request({
        url: `${apiUrl}?userId=${testUserId}`,
        method: "GET",
        header: {
          // 'X-Access-Token' 是 jeecg-boot 框架默认的 Token 键名
          // 'token' 是您调用 uni.setStorageSync 存入时的键名，请确保一致
          "X-Access-Token": common_vendor.index.getStorageSync("token")
        },
        success: (res) => {
          if (res.statusCode === 200) {
            this.messageList = res.data;
          } else {
            common_vendor.index.showToast({ title: "加载失败", icon: "none" });
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/messages/messages.vue:96", "API请求失败:", err);
          common_vendor.index.showToast({ title: "网络请求失败", icon: "none" });
        },
        complete: () => {
          this.loading = false;
          common_vendor.index.stopPullDownRefresh();
        }
      });
    },
    // 跳转到消息详情列表页
    goToDetail(appointmentId) {
      common_vendor.index.navigateTo({
        url: `/subpkg/messages/detail?appointmentId=${appointmentId}`
      });
    },
    // 格式化时间函数
    formatTime(dateTimeStr) {
      if (!dateTimeStr)
        return "";
      return dateTimeStr.split("T")[0];
    },
    // 跳转到医生端
    goDoctorMain() {
      common_vendor.index.navigateTo({
        url: "/pages/doctor/schedule/main"
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $options.groupedMessages.length > 0
  }, $options.groupedMessages.length > 0 ? {
    b: common_vendor.f($options.groupedMessages, (group, k0, i0) => {
      return {
        a: common_vendor.t($options.formatTime(group.latestMessage.createdTime)),
        b: common_vendor.t(group.latestMessage.title),
        c: group.appointmentId,
        d: common_vendor.o(($event) => $options.goToDetail(group.appointmentId), group.appointmentId)
      };
    }),
    c: common_assets._imports_0$1
  } : {
    d: common_assets._imports_1
  }, {
    e: common_vendor.o((...args) => $options.goDoctorMain && $options.goDoctorMain(...args))
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-ecc172b4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/messages/messages.js.map
