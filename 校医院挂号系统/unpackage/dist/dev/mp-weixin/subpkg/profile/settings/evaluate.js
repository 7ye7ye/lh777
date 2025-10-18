"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "evaluate",
  setup(__props) {
    const evaluateType = common_vendor.ref("门诊");
    const showEvaluateForm = common_vendor.ref(false);
    const showPatientList = common_vendor.ref(false);
    const evaluateText = common_vendor.ref("");
    const currentPatient = common_vendor.ref({
      name: "周诗晴",
      visitNo: "M017087965"
    });
    const patients = common_vendor.ref([
      { id: 1, name: "周诗晴", gender: "女", age: 28, visitNo: "M017087965" },
      { id: 2, name: "张三", gender: "男", age: 35, visitNo: "M017087966" }
    ]);
    const doctorRating = common_vendor.ref({
      attitude: 0,
      professional: 0,
      communication: 0
    });
    const hospitalRating = common_vendor.ref({
      environment: 0,
      waiting: 0
    });
    const canSubmit = common_vendor.computed(() => {
      return showEvaluateForm.value && (doctorRating.value.attitude > 0 || doctorRating.value.professional > 0 || doctorRating.value.communication > 0 || hospitalRating.value.environment > 0 || hospitalRating.value.waiting > 0 || evaluateText.value.trim().length > 0);
    });
    const onTypeChange = (e) => {
      evaluateType.value = e.detail.value;
      showEvaluateForm.value = true;
    };
    const setDoctorRating = (type, rating) => {
      doctorRating.value[type] = rating;
    };
    const setHospitalRating = (type, rating) => {
      hospitalRating.value[type] = rating;
    };
    const selectPatient = (patient) => {
      currentPatient.value = patient;
      showPatientList.value = false;
    };
    const submitEvaluate = () => {
      if (!showEvaluateForm.value) {
        showEvaluateForm.value = true;
        return;
      }
      if (!canSubmit.value) {
        common_vendor.index.showToast({ title: "请至少完成一项评价", icon: "error" });
        return;
      }
      const evaluateData = {
        type: evaluateType.value,
        patientId: currentPatient.value.id,
        doctorRating: doctorRating.value,
        hospitalRating: hospitalRating.value,
        text: evaluateText.value
      };
      api_user.userApi.submitEvaluate(evaluateData).then(() => {
        common_vendor.index.showToast({ title: "评价提交成功", icon: "success" });
        resetForm();
      }).catch(() => {
        common_vendor.index.showToast({ title: "提交失败，请重试", icon: "error" });
      });
    };
    const resetForm = () => {
      showEvaluateForm.value = false;
      evaluateText.value = "";
      doctorRating.value = { attitude: 0, professional: 0, communication: 0 };
      hospitalRating.value = { environment: 0, waiting: 0 };
    };
    common_vendor.onMounted(() => {
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(currentPatient.value.name || "周诗晴"),
        b: common_vendor.t(currentPatient.value.visitNo || "M017087965"),
        c: common_vendor.o(($event) => showPatientList.value = true),
        d: evaluateType.value === "门诊",
        e: evaluateType.value === "住院",
        f: common_vendor.o(onTypeChange),
        g: showEvaluateForm.value
      }, showEvaluateForm.value ? {
        h: common_vendor.f(5, (i, k0, i0) => {
          return {
            a: i,
            b: i <= doctorRating.value.attitude ? 1 : "",
            c: common_vendor.o(($event) => setDoctorRating("attitude", i), i)
          };
        }),
        i: common_vendor.f(5, (i, k0, i0) => {
          return {
            a: i,
            b: i <= doctorRating.value.professional ? 1 : "",
            c: common_vendor.o(($event) => setDoctorRating("professional", i), i)
          };
        }),
        j: common_vendor.f(5, (i, k0, i0) => {
          return {
            a: i,
            b: i <= doctorRating.value.communication ? 1 : "",
            c: common_vendor.o(($event) => setDoctorRating("communication", i), i)
          };
        }),
        k: common_vendor.f(5, (i, k0, i0) => {
          return {
            a: i,
            b: i <= hospitalRating.value.environment ? 1 : "",
            c: common_vendor.o(($event) => setHospitalRating("environment", i), i)
          };
        }),
        l: common_vendor.f(5, (i, k0, i0) => {
          return {
            a: i,
            b: i <= hospitalRating.value.waiting ? 1 : "",
            c: common_vendor.o(($event) => setHospitalRating("waiting", i), i)
          };
        }),
        m: evaluateText.value,
        n: common_vendor.o(($event) => evaluateText.value = $event.detail.value),
        o: common_vendor.t(evaluateText.value.length)
      } : {}, {
        p: common_vendor.t(showEvaluateForm.value ? "提交评价" : "去评价"),
        q: !canSubmit.value ? 1 : "",
        r: common_vendor.o(submitEvaluate),
        s: !canSubmit.value,
        t: showPatientList.value
      }, showPatientList.value ? {
        v: common_vendor.o(($event) => showPatientList.value = false),
        w: common_vendor.f(patients.value, (patient, k0, i0) => {
          return {
            a: common_vendor.t(patient.name),
            b: common_vendor.t(patient.gender),
            c: common_vendor.t(patient.age),
            d: patient.id,
            e: common_vendor.o(($event) => selectPatient(patient), patient.id)
          };
        })
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-91fb885c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/settings/evaluate.js.map
