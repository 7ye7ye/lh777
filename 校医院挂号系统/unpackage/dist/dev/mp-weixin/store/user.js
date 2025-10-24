"use strict";
const common_vendor = require("../common/vendor.js");
const useUserStore = common_vendor.defineStore("user", {
  state: () => ({
    token: "",
    userInfo: null,
    isLoggedIn: false
  }),
  actions: {
    setToken(token) {
      this.token = token;
      this.isLoggedIn = !!token;
      if (token) {
        common_vendor.index.setStorageSync("token", token);
      } else {
        common_vendor.index.removeStorageSync("token");
      }
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo;
      if (userInfo) {
        common_vendor.index.__f__("log", "at store/user.js:24", "store存储的userInfo:", userInfo);
        common_vendor.index.setStorageSync("userInfo", userInfo);
      } else {
        common_vendor.index.removeStorageSync("userInfo");
      }
    },
    // 初始化：从本地存储恢复状态
    initFromStorage() {
      const token = common_vendor.index.getStorageSync("token");
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      if (token) {
        this.token = token;
        this.isLoggedIn = true;
      }
      if (userInfo) {
        this.userInfo = userInfo;
      }
    },
    logout() {
      this.token = "";
      this.userInfo = null;
      this.isLoggedIn = false;
      common_vendor.index.removeStorageSync("token");
      common_vendor.index.removeStorageSync("userInfo");
    }
  }
});
exports.useUserStore = useUserStore;
//# sourceMappingURL=../../.sourcemap/mp-weixin/store/user.js.map
