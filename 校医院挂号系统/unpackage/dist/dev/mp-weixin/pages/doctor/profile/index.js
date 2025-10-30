"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const store_user = require("../../../store/user.js");
const api_doctor = require("../../../api/doctor.js");
const _sfc_main = {
  __name: "index",
  setup(__props) {
    const doctorInfo = common_vendor.ref({
      name: "",
      title: "",
      department: "",
      avatar: "/static/doctor.svg",
      phone: "",
      email: "",
      licenseNumber: "",
      yearsOfPractice: 0,
      specialty: ""
    });
    const stats = common_vendor.ref({
      totalPatients: 0,
      todayPatients: 0,
      rating: 0
    });
    const userStore = store_user.useUserStore();
    const loadProfile = async () => {
      var _a;
      try {
        userStore.initFromStorage();
        const userId = (_a = userStore.userInfo) == null ? void 0 : _a.userId;
        let profile = null;
        if (userId) {
          profile = await api_doctor.doctorApi.getProfileByUserId(userId);
        } else {
          profile = await api_doctor.doctorApi.getMyProfile();
        }
        if (!profile || !profile.doctorId) {
          utils_uniHelper.uniShowToast("未绑定医生资料或接口返回空数据");
          return;
        }
        doctorInfo.value = {
          name: profile.doctorName || profile.realname || "",
          title: profile.title || "",
          department: profile.deptName || "",
          avatar: profile.avatar || "/static/doctor.svg",
          phone: profile.phone || "",
          email: profile.email || "",
          licenseNumber: profile.licenseNumber || "",
          yearsOfPractice: profile.yearsOfPractice || 0,
          specialty: profile.specialty || ""
        };
        stats.value = {
          totalPatients: 0,
          todayPatients: 0,
          rating: 0
        };
      } catch (e) {
        utils_uniHelper.uniShowToast("获取医生资料失败");
        common_vendor.index.__f__("warn", "at pages/doctor/profile/index.vue:263", "loadProfile错误：", e);
      }
    };
    common_vendor.onMounted(loadProfile);
    function goBackToSchedule() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        common_vendor.index.navigateBack();
      } else {
        utils_uniHelper.uniNavigateTo("/pages/doctor/schedule/main");
      }
    }
    function goToScheduleManagement() {
      utils_uniHelper.uniNavigateTo("/pages/doctor/schedule/main");
    }
    function goToStatistics() {
      utils_uniHelper.uniShowToast("接诊统计功能开发中");
    }
    function goToSettings() {
      utils_uniHelper.uniNavigateTo("/pages/profile/profile");
    }
    function changePassword() {
      utils_uniHelper.uniShowToast("请前往系统设置中修改密码");
    }
    const editDialog = common_vendor.ref({
      visible: false,
      field: "",
      label: "",
      value: ""
    });
    function showEditDialog(field) {
      const labelMap = { name: "姓名", phone: "联系电话", email: "邮箱" };
      editDialog.value.visible = true;
      editDialog.value.field = field;
      editDialog.value.label = labelMap[field] || field;
      editDialog.value.value = doctorInfo.value[field] || "";
    }
    function closeEditDialog() {
      editDialog.value.visible = false;
    }
    function saveEdit() {
      const { field, value } = editDialog.value;
      if (field === "name" && !value.trim()) {
        utils_uniHelper.uniShowToast("姓名不能为空");
        return;
      }
      if (field === "phone" && !/^1[3-9]\d{9}$/.test(value)) {
        utils_uniHelper.uniShowToast("请输入有效手机号");
        return;
      }
      if (field === "email" && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        utils_uniHelper.uniShowToast("请输入有效邮箱");
        return;
      }
      doctorInfo.value[field] = value;
      closeEditDialog();
      utils_uniHelper.uniShowToast("已保存");
    }
    function logout() {
      common_vendor.index.showModal({
        title: "退出登录",
        content: "确认退出登录？",
        success: (res) => {
          if (res.confirm) {
            try {
              common_vendor.index.clearStorage();
            } catch (e) {
            }
            common_vendor.index.reLaunch({ url: "/subpkg/auth/login" });
          }
        }
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBackToSchedule),
        b: doctorInfo.value.avatar,
        c: common_vendor.t(doctorInfo.value.name),
        d: common_vendor.t(doctorInfo.value.title),
        e: common_vendor.t(doctorInfo.value.department),
        f: common_vendor.t(stats.value.totalPatients),
        g: common_vendor.t(stats.value.todayPatients),
        h: common_vendor.t(stats.value.rating),
        i: common_vendor.t(doctorInfo.value.name),
        j: common_vendor.o(($event) => showEditDialog("name")),
        k: common_vendor.t(doctorInfo.value.department),
        l: common_vendor.t(doctorInfo.value.title),
        m: common_vendor.t(doctorInfo.value.phone),
        n: common_vendor.o(($event) => showEditDialog("phone")),
        o: common_vendor.t(doctorInfo.value.email),
        p: common_vendor.o(($event) => showEditDialog("email")),
        q: common_vendor.t(doctorInfo.value.licenseNumber),
        r: common_vendor.t(doctorInfo.value.yearsOfPractice),
        s: common_vendor.t(doctorInfo.value.specialty),
        t: common_vendor.o(goToScheduleManagement),
        v: common_vendor.o(goToStatistics),
        w: common_vendor.o(goToSettings),
        x: common_vendor.o(changePassword),
        y: common_vendor.o(logout),
        z: editDialog.value.visible
      }, editDialog.value.visible ? {
        A: common_vendor.t(editDialog.value.label),
        B: `请输入${editDialog.value.label}`,
        C: editDialog.value.value,
        D: common_vendor.o(($event) => editDialog.value.value = $event.detail.value),
        E: common_vendor.o(closeEditDialog),
        F: common_vendor.o(saveEdit),
        G: common_vendor.o(closeEditDialog)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-b5233218"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/profile/index.js.map
