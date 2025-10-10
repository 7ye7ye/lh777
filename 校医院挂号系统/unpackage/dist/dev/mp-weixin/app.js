"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/home/home.js";
  "./pages/assistant/assistant.js";
  "./pages/messages/messages.js";
  "./pages/hospital/hospital.js";
  "./pages/hospital/departments.js";
  "./pages/hospital/department-detail.js";
  "./pages/profile/profile.js";
  "./pages/login/login.js";
  "./pages/register/register.js";
  "./pages/doctor/schedule/schedule.js";
  "./pages/doctor/schedule/apply-change.js";
  "./pages/doctor/patients/patient-list.js";
  "./pages/doctor/patients/patient-detail.js";
  "./pages/doctor/profile/doctor-profile.js";
  "./pages/profile/personal/mycard.js";
  "./pages/profile/personal/mypatient.js";
  "./pages/profile/personal/mydoctor.js";
  "./pages/profile/records/register-record.js";
  "./pages/profile/records/outpatient-record.js";
  "./pages/profile/records/hospital-record.js";
  "./pages/profile/records/consult-record.js";
  "./pages/profile/records/revisit-record.js";
  "./pages/profile/records/check-record.js";
  "./pages/profile/settings/privacy.js";
  "./pages/profile/help/help.js";
  "./pages/profile/settings/complain.js";
  "./pages/profile/settings/evaluate.js";
  "./pages/profile/settings/unbind.js";
  "./pages/profile/help/login.js";
  "./pages/profile/help/bindcard.js";
  "./pages/profile/help/register.js";
  "./pages/profile/help/payment.js";
  "./pages/profile/help/hospitalization.js";
  "./pages/profile/help/report.js";
  "./pages/profile/help/other.js";
  "./pages/profile/help/appointment.js";
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
