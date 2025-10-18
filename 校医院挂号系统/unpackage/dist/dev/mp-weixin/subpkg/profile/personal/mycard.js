"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const store_user = require("../../../store/user.js");
const _sfc_main = {
  __name: "mycard",
  setup(__props) {
    const cardInfo = common_vendor.ref({});
    store_user.useUserStore();
    const getCardInfo = () => {
      api_user.userApi.getCard().then((res) => {
        cardInfo.value = res.data || {};
      }).catch(() => {
        cardInfo.value = {
          name: "张三",
          cardNumber: "M017080045",
          idType: "身份证",
          idNumber: "42012319900101961",
          phone: "15812345678",
          address: "北京市北京市海淀区北京交通大学"
        };
      });
    };
    const maskIdNumber = (idNumber) => {
      if (!idNumber)
        return "";
      if (idNumber.length < 8)
        return idNumber;
      return idNumber.substring(0, 3) + "**********" + idNumber.substring(idNumber.length - 3);
    };
    const maskPhone = (phone) => {
      if (!phone)
        return "";
      if (phone.length < 7)
        return phone;
      return phone.substring(0, 2) + "******" + phone.substring(phone.length - 3);
    };
    const goToModifyInfo = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/profile/personal/modify-info" });
    };
    const goToReplaceCard = () => {
      common_vendor.index.showModal({
        title: "更换就诊卡",
        content: "更换新就诊卡功能开发中，敬请期待",
        showCancel: false
      });
    };
    common_vendor.onMounted(() => {
      getCardInfo();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(cardInfo.value.name || "张三"),
        b: common_vendor.t(cardInfo.value.cardNumber || "M017080045"),
        c: common_vendor.t(cardInfo.value.idType || "身份证"),
        d: common_vendor.t(maskIdNumber(cardInfo.value.idNumber) || "420**********961"),
        e: common_vendor.t(maskPhone(cardInfo.value.phone) || "15******467"),
        f: common_vendor.t(cardInfo.value.address || "北京市北京市海淀区北京交通大学"),
        g: common_vendor.o(goToModifyInfo),
        h: common_vendor.o(goToReplaceCard)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-63fdac57"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/personal/mycard.js.map
