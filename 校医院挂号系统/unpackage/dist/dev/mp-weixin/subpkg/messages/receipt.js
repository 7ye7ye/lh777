"use strict";
const common_vendor = require("../../common/vendor.js");
const uni_modules_SansnnUQRCode_js_sdk_uqrcode_uqrcode = require("../../uni_modules/Sansnn-uQRCode/js_sdk/uqrcode/uqrcode.js");
const _sfc_main = {
  data() {
    return {
      appointmentId: null,
      receiptDetail: null
      // 存储回执单详情
    };
  },
  onLoad(options) {
    if (options.id) {
      this.appointmentId = options.id;
      this.fetchReceiptDetail();
    }
  },
  methods: {
    fetchReceiptDetail() {
      const apiUrl = `http://10.61.62.249:8095/jeecg-boot/api/appointment/detail`;
      common_vendor.index.request({
        url: `${apiUrl}?id=${this.appointmentId}`,
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            this.receiptDetail = res.data;
            this.$nextTick(() => {
              this.generateQRCode(this.receiptDetail.qrCodeData);
            });
          }
        }
      });
    },
    // 生成二维码
    // 新版本的 generateQRCode 方法
    generateQRCode(text) {
      common_vendor.index.createSelectorQuery().in(this).select("#qrcode").fields({ node: true, size: true }).exec((res) => {
        if (res && res[0] && res[0].node) {
          const canvas = res[0].node;
          const ctx = canvas.getContext("2d");
          canvas.width = res[0].width;
          canvas.height = res[0].height;
          const qr = new uni_modules_SansnnUQRCode_js_sdk_uqrcode_uqrcode.b({
            data: text,
            size: res[0].width,
            margin: 10,
            backgroundColor: "#ffffff",
            foregroundColor: "#000000"
          }, ctx);
          qr.make();
          qr.drawCanvas();
        } else {
          common_vendor.index.__f__("error", "at subpkg/messages/receipt.vue:117", "无法找到 canvas 节点，请检查 canvas-id 是否正确以及 DOM 是否已渲染。");
          common_vendor.index.showToast({
            title: "二维码生成失败",
            icon: "none"
          });
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.receiptDetail
  }, $data.receiptDetail ? {
    b: common_vendor.t($data.receiptDetail.qrCodeData),
    c: common_vendor.t($data.receiptDetail.patientName),
    d: common_vendor.t($data.receiptDetail.hospitalAddress),
    e: common_vendor.t($data.receiptDetail.departmentName),
    f: common_vendor.t($data.receiptDetail.visitLocation),
    g: common_vendor.t($data.receiptDetail.doctorName),
    h: common_vendor.t($data.receiptDetail.appointmentDate),
    i: common_vendor.t($data.receiptDetail.appointmentTime),
    j: common_vendor.t($data.receiptDetail.consultationFee),
    k: common_vendor.t($data.receiptDetail.status),
    l: common_vendor.t($data.receiptDetail.orderNumber)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-41615fcc"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subpkg/messages/receipt.js.map
