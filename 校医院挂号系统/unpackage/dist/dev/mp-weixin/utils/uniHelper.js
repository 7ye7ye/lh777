"use strict";
const common_vendor = require("../common/vendor.js");
const uniNavigateTo = (options) => {
  return new Promise((resolve, reject) => {
    common_vendor.index.navigateTo({
      ...options,
      success: resolve,
      fail: reject
    });
  });
};
const uniNavigateBack = (delta = 1) => {
  common_vendor.index.navigateBack({
    delta
  });
};
const uniShowToast = (options) => {
  return new Promise((resolve) => {
    common_vendor.index.showToast({
      icon: "none",
      duration: 2e3,
      ...options,
      success: resolve
    });
  });
};
const uniSwitchTab = (options) => {
  return new Promise((resolve, reject) => {
    common_vendor.index.switchTab({
      ...options,
      success: resolve,
      fail: reject
    });
  });
};
exports.uniNavigateBack = uniNavigateBack;
exports.uniNavigateTo = uniNavigateTo;
exports.uniShowToast = uniShowToast;
exports.uniSwitchTab = uniSwitchTab;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/uniHelper.js.map
