"use strict";
const common_vendor = require("../common/vendor.js");
function uniShowModal(options) {
  common_vendor.index.showModal(options);
}
function uniShowToast(options) {
  common_vendor.index.showToast(options);
}
function uniNavigateTo(options) {
  common_vendor.index.navigateTo(options);
}
function uniSwitchTab(options) {
  common_vendor.index.switchTab(options);
}
exports.uniNavigateTo = uniNavigateTo;
exports.uniShowModal = uniShowModal;
exports.uniShowToast = uniShowToast;
exports.uniSwitchTab = uniSwitchTab;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/uniHelper.js.map
