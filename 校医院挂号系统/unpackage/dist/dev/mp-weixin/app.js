"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/home/home.js";
  "./pages/assistant/assistant.js";
  "./pages/messages/messages.js";
  "./pages/doctor/schedule/main.js";
  "./pages/doctor/schedule/apply.js";
  "./pages/doctor/patients/list.js";
  "./pages/doctor/patients/detail.js";
  "./pages/doctor/profile/index.js";
  "./pages/hospital/hospital.js";
  "./pages/profile/profile.js";
  "./pages/messages/login.js";
  "./pages/messages/register.js";
  "./subpkg/auth/login.js";
  "./subpkg/auth/register.js";
}
const _sfc_main = {
  onLaunch: function() {
    common_vendor.index.__f__("log", "at App.vue:4", "App Launch");
  },
  onShow: function() {
    common_vendor.index.__f__("log", "at App.vue:7", "App Show");
  },
  onHide: function() {
    common_vendor.index.__f__("log", "at App.vue:10", "App Hide");
  }
};
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  const pinia = common_vendor.createPinia();
  app.use(pinia);
  const { useUserStore } = require("./store/user.js");
  const userStore = useUserStore();
  userStore.initFromStorage();
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map
