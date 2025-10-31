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
  "./subpkg/profile/personal/modify-info.js";
  "./subpkg/profile/personal/mycard.js";
  "./subpkg/profile/personal/create-card.js";
  "./subpkg/profile/personal/mypatient.js";
  "./subpkg/profile/personal/mydoctor.js";
  "./subpkg/profile/records/register-record.js";
  "./subpkg/profile/records/outpatient-record.js";
  "./subpkg/profile/records/hospital-record.js";
  "./subpkg/profile/records/consult-record.js";
  "./subpkg/profile/records/revisit-record.js";
  "./subpkg/profile/records/check-record.js";
  "./subpkg/profile/settings/privacy.js";
  "./subpkg/profile/help/help.js";
  "./subpkg/profile/settings/complain.js";
  "./subpkg/profile/settings/evaluate.js";
  "./subpkg/profile/settings/unbind.js";
  "./subpkg/profile/help/login.js";
  "./subpkg/profile/help/bindcard.js";
  "./subpkg/profile/help/register.js";
  "./subpkg/profile/help/payment.js";
  "./subpkg/profile/help/hospitalization.js";
  "./subpkg/profile/help/report.js";
  "./subpkg/profile/help/other.js";
  "./subpkg/hospital/departments.js";
  "./subpkg/hospital/department-detail.js";
  "./subpkg/messages/detail.js";
  "./subpkg/messages/receipt.js";
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
