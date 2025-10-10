"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "complain",
  setup(__props) {
    const activeTab = common_vendor.ref("feedback");
    const selectedCategory = common_vendor.ref("投诉");
    const selectedBusiness = common_vendor.ref("门诊");
    const feedbackContent = common_vendor.ref("");
    const selectedCampus = common_vendor.ref("");
    const showCampusModal = common_vendor.ref(false);
    const userInfo = common_vendor.ref({});
    const problemCategories = [
      { label: "举报", value: "举报" },
      { label: "投诉", value: "投诉" },
      { label: "建议", value: "建议" },
      { label: "反馈", value: "反馈" },
      { label: "咨询", value: "咨询" }
    ];
    const businessTypes = [
      { label: "门诊", value: "门诊" },
      { label: "住院", value: "住院" },
      { label: "收费", value: "收费" },
      { label: "检查", value: "检查" },
      { label: "检验", value: "检验" },
      { label: "药房", value: "药房" },
      { label: "问诊", value: "问诊" },
      { label: "其他", value: "其他" }
    ];
    const campuses = [
      { label: "总院区", value: "总院区" },
      { label: "东院区", value: "东院区" },
      { label: "西院区", value: "西院区" },
      { label: "南院区", value: "南院区" },
      { label: "北院区", value: "北院区" }
    ];
    const myFeedbacks = common_vendor.ref([
      {
        id: 1,
        category: "投诉",
        business: "门诊",
        content: "挂号排队时间过长，希望能优化流程",
        time: "2025-09-20 14:30",
        status: "processing",
        statusText: "处理中"
      },
      {
        id: 2,
        category: "建议",
        business: "收费",
        content: "建议增加移动支付方式",
        time: "2025-09-19 10:15",
        status: "completed",
        statusText: "已处理"
      }
    ]);
    const canSubmit = common_vendor.computed(() => {
      return selectedCategory.value && selectedBusiness.value && feedbackContent.value.trim().length > 0 && selectedCampus.value;
    });
    const getPlaceholder = () => {
      const placeholders = {
        "举报": "请填写您的举报内容",
        "投诉": "请填写您的投诉内容",
        "建议": "请填写您的建议内容",
        "反馈": "请填写您的反馈内容",
        "咨询": "请填写您的咨询内容"
      };
      return placeholders[selectedCategory.value] || "请填写您的反馈内容";
    };
    const goBack = () => {
      common_vendor.index.navigateBack();
    };
    const goHome = () => {
      common_vendor.index.reLaunch({ url: "/pages/home/home" });
    };
    const switchTab = (tab) => {
      activeTab.value = tab;
    };
    const selectCategory = (category) => {
      selectedCategory.value = category;
    };
    const selectBusiness = (business) => {
      selectedBusiness.value = business;
    };
    const onContentInput = () => {
    };
    const selectCampus = () => {
      showCampusModal.value = true;
    };
    const selectCampusItem = (campus) => {
      selectedCampus.value = campus;
      showCampusModal.value = false;
    };
    const submitFeedback = () => {
      if (!canSubmit.value) {
        common_vendor.index.showToast({ title: "请完善所有必填信息", icon: "error" });
        return;
      }
      const feedbackData = {
        category: selectedCategory.value,
        business: selectedBusiness.value,
        content: feedbackContent.value,
        campus: selectedCampus.value,
        user: userInfo.value.name || "周诗晴"
      };
      api_user.userApi.submitComplain(feedbackData).then(() => {
        common_vendor.index.showToast({ title: "反馈提交成功", icon: "success" });
        feedbackContent.value = "";
        selectedCampus.value = "";
        setTimeout(() => {
          activeTab.value = "myfeedback";
        }, 1500);
      }).catch(() => {
        common_vendor.index.showToast({ title: "提交失败，请重试", icon: "error" });
      });
    };
    const getUserInfo = () => {
      api_user.userApi.getCurrentUser().then((res) => {
        userInfo.value = res.data || {};
      }).catch(() => {
        userInfo.value = { name: "周诗晴" };
      });
    };
    common_vendor.onMounted(() => {
      getUserInfo();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.o(goHome),
        c: activeTab.value === "feedback"
      }, activeTab.value === "feedback" ? {} : {}, {
        d: activeTab.value === "feedback" ? 1 : "",
        e: common_vendor.o(($event) => switchTab("feedback")),
        f: activeTab.value === "myfeedback"
      }, activeTab.value === "myfeedback" ? {} : {}, {
        g: activeTab.value === "myfeedback" ? 1 : "",
        h: common_vendor.o(($event) => switchTab("myfeedback")),
        i: activeTab.value === "feedback"
      }, activeTab.value === "feedback" ? {
        j: common_vendor.f(problemCategories, (category, k0, i0) => {
          return {
            a: common_vendor.t(category.label),
            b: category.value,
            c: selectedCategory.value === category.value ? 1 : "",
            d: common_vendor.o(($event) => selectCategory(category.value), category.value)
          };
        }),
        k: common_vendor.f(businessTypes, (business, k0, i0) => {
          return {
            a: common_vendor.t(business.label),
            b: business.value,
            c: selectedBusiness.value === business.value ? 1 : "",
            d: common_vendor.o(($event) => selectBusiness(business.value), business.value)
          };
        }),
        l: getPlaceholder(),
        m: common_vendor.o([($event) => feedbackContent.value = $event.detail.value, onContentInput]),
        n: feedbackContent.value,
        o: common_vendor.t(feedbackContent.value.length),
        p: common_vendor.t(userInfo.value.name || "周诗晴"),
        q: common_vendor.t(selectedCampus.value || "请选择院区"),
        r: common_vendor.o(selectCampus),
        s: !canSubmit.value ? 1 : "",
        t: common_vendor.o(submitFeedback),
        v: !canSubmit.value
      } : {}, {
        w: activeTab.value === "myfeedback"
      }, activeTab.value === "myfeedback" ? common_vendor.e({
        x: common_vendor.f(myFeedbacks.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.category),
            b: common_vendor.t(item.statusText),
            c: common_vendor.n(item.status),
            d: common_vendor.t(item.content),
            e: common_vendor.t(item.time),
            f: common_vendor.t(item.business),
            g: item.id
          };
        }),
        y: myFeedbacks.value.length === 0
      }, myFeedbacks.value.length === 0 ? {} : {}) : {}, {
        z: showCampusModal.value
      }, showCampusModal.value ? {
        A: common_vendor.o(($event) => showCampusModal.value = false),
        B: common_vendor.f(campuses, (campus, k0, i0) => {
          return {
            a: common_vendor.t(campus.label),
            b: campus.value,
            c: common_vendor.o(($event) => selectCampusItem(campus.value), campus.value)
          };
        })
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-3b1ce0a3"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/profile/settings/complain.js.map
