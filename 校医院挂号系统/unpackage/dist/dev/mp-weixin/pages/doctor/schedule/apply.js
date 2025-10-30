"use strict";
const common_vendor = require("../../../common/vendor.js");
const utils_uniHelper = require("../../../utils/uniHelper.js");
const store_user = require("../../../store/user.js");
const api_doctor = require("../../../api/doctor.js");
const _sfc_main = {
  __name: "apply",
  setup(__props) {
    const userStore = store_user.useUserStore();
    common_vendor.computed(() => {
      var _a;
      return ((_a = userStore.userInfo) == null ? void 0 : _a.id) || 1;
    });
    const slots = [
      { value: 1, label: "上午" },
      { value: 2, label: "下午" },
      { value: 3, label: "晚上" }
    ];
    const form = common_vendor.ref({
      originalScheduleId: void 0,
      targetDate: "",
      targetTimeSlot: 1,
      targetDeptId: void 0,
      targetDeptName: "",
      // 辅助展示
      reason: ""
    });
    const loading = common_vendor.ref(false);
    const disabled = common_vendor.computed(
      () => !form.value.originalScheduleId || !form.value.targetDate || !form.value.targetTimeSlot || !form.value.targetDeptId || !form.value.reason
    );
    const onDateChange = (e, which) => {
      var _a;
      const val = ((_a = e == null ? void 0 : e.detail) == null ? void 0 : _a.value) || "";
      if (which === "target")
        form.value.targetDate = val;
    };
    const slotLabel = (s) => {
      var _a;
      return ((_a = slots.find((x) => x.value === s)) == null ? void 0 : _a.label) || "-";
    };
    const statusMap = { 1: "待审批", 2: "已通过", 3: "已驳回", 4: "已撤销" };
    const statusOptions = ["全部", "待审批", "已通过", "已驳回", "已撤销"];
    const statusText = common_vendor.ref("全部");
    const statusFilter = common_vendor.ref(void 0);
    const onStatusFilter = (e) => {
      var _a;
      const idx = Number(((_a = e == null ? void 0 : e.detail) == null ? void 0 : _a.value) ?? 0);
      statusText.value = statusOptions[idx];
      statusFilter.value = idx === 0 ? void 0 : idx;
      loadList();
    };
    const list = common_vendor.ref([]);
    const loadList = async () => {
      var _a;
      try {
        const userStore2 = store_user.useUserStore();
        await userStore2.initFromStorage();
        const doctorId = ((_a = userStore2.userInfo) == null ? void 0 : _a.id) || 1;
        const data = await api_doctor.doctorApi.listShiftChange(doctorId, statusFilter.value);
        list.value = Array.isArray(data) ? data : [];
      } catch (e) {
        list.value = [];
      }
    };
    const onSubmit = async () => {
      var _a;
      if (disabled.value) {
        await utils_uniHelper.uniShowToast({ title: "请完整填写信息", icon: "none" });
        return;
      }
      loading.value = true;
      try {
        const userStore2 = store_user.useUserStore();
        await userStore2.initFromStorage();
        const doctorId = ((_a = userStore2.userInfo) == null ? void 0 : _a.id) || 1;
        const payload = {
          doctorId,
          originalScheduleId: form.value.originalScheduleId,
          targetDate: form.value.targetDate,
          targetTimeSlot: form.value.targetTimeSlot,
          targetDeptId: form.value.targetDeptId,
          reason: form.value.reason
        };
        await api_doctor.doctorApi.applyShiftChange(payload);
        await utils_uniHelper.uniShowToast({ title: "提交成功" });
        form.value.reason = "";
        await loadList();
      } catch (e) {
        await utils_uniHelper.uniShowToast({ title: e && e.message || "提交失败", icon: "none" });
      } finally {
        loading.value = false;
      }
    };
    common_vendor.onMounted(() => {
      const userStore2 = store_user.useUserStore();
      userStore2.initFromStorage();
      loadList();
    });
    const statusClass = (s) => {
      if (s === 0 || s === 1)
        return "audit";
      if (s === 2)
        return "pass";
      if (s === 3)
        return "reject";
      if (s === 4)
        return "cancel";
      return "";
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: form.value.originalScheduleId,
        b: common_vendor.o(common_vendor.m(($event) => form.value.originalScheduleId = $event.detail.value, {
          number: true
        })),
        c: common_vendor.t(form.value.targetDate || "请选择日期"),
        d: form.value.targetDate,
        e: common_vendor.o(($event) => onDateChange($event, "target")),
        f: common_vendor.f(slots, (slot, k0, i0) => {
          return {
            a: common_vendor.t(slot.label),
            b: "target-" + slot.value,
            c: form.value.targetTimeSlot === slot.value ? 1 : "",
            d: common_vendor.o(($event) => form.value.targetTimeSlot = slot.value, "target-" + slot.value)
          };
        }),
        g: form.value.targetDeptId,
        h: common_vendor.o(common_vendor.m(($event) => form.value.targetDeptId = $event.detail.value, {
          number: true
        })),
        i: form.value.targetDeptName,
        j: common_vendor.o(common_vendor.m(($event) => form.value.targetDeptName = $event.detail.value, {
          trim: true
        })),
        k: form.value.reason,
        l: common_vendor.o(common_vendor.m(($event) => form.value.reason = $event.detail.value, {
          trim: true
        })),
        m: disabled.value || loading.value,
        n: loading.value,
        o: common_vendor.o(onSubmit),
        p: common_vendor.t(statusText.value),
        q: statusOptions,
        r: common_vendor.o(onStatusFilter),
        s: list.value.length === 0
      }, list.value.length === 0 ? {} : {
        t: common_vendor.f(list.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(statusMap[item.status] || "未知"),
            b: common_vendor.n(statusClass(item.status)),
            c: common_vendor.t((item.applyTime || "").replace("T", " ")),
            d: common_vendor.t(item.originalScheduleId),
            e: common_vendor.t(item.targetDate),
            f: common_vendor.t(slotLabel(item.targetTimeSlot)),
            g: common_vendor.t(item.targetDeptId),
            h: common_vendor.t(item.reason),
            i: item.adjustmentId
          };
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-cacad53e"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/doctor/schedule/apply.js.map
