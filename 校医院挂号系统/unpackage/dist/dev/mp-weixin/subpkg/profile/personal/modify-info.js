"use strict";
const common_vendor = require("../../../common/vendor.js");
require("../../../utils/request.js");
const store_user = require("../../../store/user.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const _sfc_main = {
  __name: "modify-info",
  setup(__props) {
    const userStore = store_user.useUserStore();
    const loading = common_vendor.ref(false);
    const countdown = common_vendor.ref(0);
    const formData = common_vendor.reactive({
      name: "",
      idType: "身份证",
      idNumber: "",
      gender: "女",
      birthDate: "2004-09-29",
      nation: "汉族",
      nationality: "中国",
      region: "北京市北京市海淀区",
      address: "北京交通大学",
      phone: "",
      verificationCode: ""
    });
    const idTypeOptions = ["身份证", "护照", "军官证", "其他"];
    const genderOptions = ["男", "女"];
    const nationOptions = ["汉族", "蒙古族", "回族", "藏族", "维吾尔族", "苗族", "彝族", "壮族", "布依族", "朝鲜族", "满族", "侗族", "瑶族", "白族", "土家族", "哈尼族", "哈萨克族", "傣族", "黎族", "傈僳族", "佤族", "畲族", "高山族", "拉祜族", "水族", "东乡族", "纳西族", "景颇族", "柯尔克孜族", "土族", "达斡尔族", "仫佬族", "羌族", "布朗族", "撒拉族", "毛南族", "仡佬族", "锡伯族", "阿昌族", "普米族", "塔吉克族", "怒族", "乌孜别克族", "俄罗斯族", "鄂温克族", "德昂族", "保安族", "裕固族", "京族", "塔塔尔族", "独龙族", "鄂伦春族", "赫哲族", "门巴族", "珞巴族", "基诺族"];
    const nationalityOptions = ["中国", "美国", "英国", "日本", "韩国", "其他"];
    const idTypeIndex = common_vendor.ref(0);
    const genderIndex = common_vendor.ref(1);
    const nationIndex = common_vendor.ref(0);
    const nationalityIndex = common_vendor.ref(0);
    const regionValue = common_vendor.ref(["北京市", "北京市", "海淀区"]);
    const onIdTypeChange = (e) => {
      idTypeIndex.value = e.detail.value;
      formData.idType = idTypeOptions[e.detail.value];
    };
    const onGenderChange = (e) => {
      genderIndex.value = e.detail.value;
      formData.gender = genderOptions[e.detail.value];
    };
    const onBirthDateChange = (e) => {
      formData.birthDate = e.detail.value;
    };
    const onNationChange = (e) => {
      nationIndex.value = e.detail.value;
      formData.nation = nationOptions[e.detail.value];
    };
    const onNationalityChange = (e) => {
      nationalityIndex.value = e.detail.value;
      formData.nationality = nationalityOptions[e.detail.value];
    };
    const onRegionChange = (e) => {
      regionValue.value = e.detail.value;
      formData.region = e.detail.value.join("");
    };
    const getVerificationCode = () => {
      if (!formData.phone) {
        utils_uniHelper.uniShowToast({ title: "请先输入手机号", icon: "none" });
        return;
      }
      utils_uniHelper.uniShowToast({ title: "验证码已发送", icon: "success" });
      countdown.value = 60;
      const timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1e3);
    };
    const submitForm = async () => {
      if (!formData.name) {
        utils_uniHelper.uniShowToast({ title: "请输入姓名", icon: "none" });
        return;
      }
      if (!formData.verificationCode) {
        utils_uniHelper.uniShowToast({ title: "请输入验证码", icon: "none" });
        return;
      }
      loading.value = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 1e3));
        utils_uniHelper.uniShowToast({ title: "修改成功", icon: "success" });
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 1500);
      } catch (e) {
        utils_uniHelper.uniShowToast({ title: "修改失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    common_vendor.onMounted(() => {
      if (userStore.userInfo) {
        formData.name = userStore.userInfo.name || "";
        formData.phone = userStore.userInfo.phone || "";
      }
    });
    return (_ctx, _cache) => {
      return {
        a: formData.name,
        b: common_vendor.o(($event) => formData.name = $event.detail.value),
        c: common_vendor.t(formData.idType || "请选择证件类型"),
        d: common_vendor.o(onIdTypeChange),
        e: idTypeIndex.value,
        f: idTypeOptions,
        g: formData.idNumber,
        h: common_vendor.o(($event) => formData.idNumber = $event.detail.value),
        i: common_vendor.t(formData.gender || "请选择性别"),
        j: common_vendor.o(onGenderChange),
        k: genderIndex.value,
        l: genderOptions,
        m: common_vendor.t(formData.birthDate || "请选择出生日期"),
        n: common_vendor.o(onBirthDateChange),
        o: formData.birthDate,
        p: common_vendor.t(formData.nation || "请选择民族"),
        q: common_vendor.o(onNationChange),
        r: nationIndex.value,
        s: nationOptions,
        t: common_vendor.t(formData.nationality || "请选择国籍"),
        v: common_vendor.o(onNationalityChange),
        w: nationalityIndex.value,
        x: nationalityOptions,
        y: common_vendor.t(formData.region || "请选择所在地区"),
        z: common_vendor.o(onRegionChange),
        A: regionValue.value,
        B: formData.address,
        C: common_vendor.o(($event) => formData.address = $event.detail.value),
        D: formData.phone,
        E: common_vendor.o(($event) => formData.phone = $event.detail.value),
        F: formData.verificationCode,
        G: common_vendor.o(($event) => formData.verificationCode = $event.detail.value),
        H: common_vendor.t(countdown.value > 0 ? `${countdown.value}s` : "获取验证码"),
        I: common_vendor.o(getVerificationCode),
        J: countdown.value > 0,
        K: common_vendor.o(submitForm),
        L: loading.value
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d7bbeb84"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/personal/modify-info.js.map
