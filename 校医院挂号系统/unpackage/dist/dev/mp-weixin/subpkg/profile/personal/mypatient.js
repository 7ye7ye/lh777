"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "mypatient",
  setup(__props) {
    const patients = common_vendor.ref([
      {
        id: 1,
        name: "张三",
        gender: "男",
        age: 35,
        idType: "身份证",
        idNumber: "110101199001011234",
        phone: "138****5678",
        birthday: "1990-01-01"
      },
      {
        id: 2,
        name: "李四",
        gender: "女",
        age: 28,
        idType: "身份证",
        idNumber: "110101199501011234",
        phone: "139****5678",
        birthday: "1995-01-01"
      }
    ]);
    const showAddForm = common_vendor.ref(false);
    const isEdit = common_vendor.ref(false);
    const idTypes = ["身份证", "护照"];
    const genders = ["男", "女"];
    const formData = common_vendor.ref({
      name: "",
      idType: "身份证",
      idNumber: "",
      gender: "男",
      birthday: "请选择",
      phone: ""
    });
    const onTypeChange = (e) => {
      formData.value.idType = idTypes[e.detail.value];
    };
    const onGenderChange = (e) => {
      formData.value.gender = genders[e.detail.value];
    };
    const onDateChange = (e) => {
      formData.value.birthday = e.detail.value;
    };
    const editPatient = (patient) => {
      isEdit.value = true;
      formData.value = { ...patient };
      showAddForm.value = true;
    };
    const deletePatient = (id) => {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这个就诊人吗？",
        success: (res) => {
          if (res.confirm) {
            patients.value = patients.value.filter((p) => p.id !== id);
            common_vendor.index.showToast({ title: "删除成功", icon: "success" });
          }
        }
      });
    };
    const closeForm = () => {
      showAddForm.value = false;
      isEdit.value = false;
      resetForm();
    };
    const resetForm = () => {
      formData.value = {
        name: "",
        idType: "身份证",
        idNumber: "",
        gender: "男",
        birthday: "请选择",
        phone: ""
      };
    };
    const submitForm = () => {
      if (!formData.value.name || !formData.value.idNumber || !formData.value.phone) {
        common_vendor.index.showToast({ title: "请填写完整信息", icon: "error" });
        return;
      }
      if (isEdit.value) {
        const index = patients.value.findIndex((p) => p.id === formData.value.id);
        if (index !== -1) {
          patients.value[index] = { ...formData.value };
        }
        common_vendor.index.showToast({ title: "编辑成功", icon: "success" });
      } else {
        const newPatient = {
          ...formData.value,
          id: Date.now(),
          age: (/* @__PURE__ */ new Date()).getFullYear() - new Date(formData.value.birthday).getFullYear()
        };
        patients.value.push(newPatient);
        common_vendor.index.showToast({ title: "添加成功", icon: "success" });
      }
      closeForm();
    };
    const getPatientList = () => {
      api_user.userApi.getPatientList().then((res) => {
      }).catch(() => {
      });
    };
    common_vendor.onMounted(() => {
      getPatientList();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(($event) => showAddForm.value = true),
        b: common_vendor.f(patients.value, (patient, k0, i0) => {
          return {
            a: common_vendor.t(patient.name),
            b: common_vendor.t(patient.gender),
            c: common_vendor.t(patient.age),
            d: common_vendor.t(patient.idType),
            e: common_vendor.t(patient.idNumber),
            f: common_vendor.t(patient.phone),
            g: common_vendor.o(($event) => editPatient(patient), patient.id),
            h: common_vendor.o(($event) => deletePatient(patient.id), patient.id),
            i: patient.id
          };
        }),
        c: showAddForm.value
      }, showAddForm.value ? {
        d: common_vendor.t(isEdit.value ? "编辑就诊人" : "添加就诊人"),
        e: common_vendor.o(closeForm),
        f: formData.value.name,
        g: common_vendor.o(($event) => formData.value.name = $event.detail.value),
        h: common_vendor.t(formData.value.idType),
        i: idTypes,
        j: common_vendor.o(onTypeChange),
        k: formData.value.idNumber,
        l: common_vendor.o(($event) => formData.value.idNumber = $event.detail.value),
        m: common_vendor.t(formData.value.gender),
        n: genders,
        o: common_vendor.o(onGenderChange),
        p: common_vendor.t(formData.value.birthday),
        q: common_vendor.o(onDateChange),
        r: formData.value.phone,
        s: common_vendor.o(($event) => formData.value.phone = $event.detail.value),
        t: common_vendor.o(closeForm),
        v: common_vendor.o(submitForm)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ecb493c2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/subpkg/profile/personal/mypatient.js.map
