"use strict";
const common_vendor = require("../../../common/vendor.js");
const api_user = require("../../../api/user.js");
const _sfc_main = {
  __name: "mydoctor",
  setup(__props) {
    const doctors = common_vendor.ref([
      {
        id: 1,
        name: "李医生",
        dept: "内科",
        avatar: "/static/avatar.png",
        title: "主任医师",
        introduction: "擅长心血管疾病的诊断和治疗",
        tags: ["心血管", "高血压", "糖尿病"]
      },
      {
        id: 2,
        name: "王医生",
        dept: "外科",
        avatar: "/static/avatar.png",
        title: "副主任医师",
        introduction: "专业从事微创外科手术",
        tags: ["微创手术", "腹腔镜", "甲状腺"]
      },
      {
        id: 3,
        name: "张医生",
        dept: "儿科",
        avatar: "/static/avatar.png",
        title: "主治医师",
        introduction: "儿童常见病多发病的诊治",
        tags: ["儿童保健", "呼吸道感染", "消化系统"]
      }
    ]);
    const searchKeyword = common_vendor.ref("");
    const filteredDoctors = common_vendor.computed(() => {
      if (!searchKeyword.value) {
        return doctors.value;
      }
      const keyword = searchKeyword.value.toLowerCase();
      return doctors.value.filter(
        (doctor) => doctor.name.toLowerCase().includes(keyword) || doctor.dept.toLowerCase().includes(keyword) || doctor.introduction.toLowerCase().includes(keyword) || doctor.tags.some((tag) => tag.toLowerCase().includes(keyword))
      );
    });
    const getDoctorList = () => {
      api_user.userApi.getDoctorList().then((res) => {
        common_vendor.index.showToast({ title: "获取成功", icon: "success" });
      }).catch(() => {
        common_vendor.index.showToast({ title: "获取失败", icon: "error" });
      });
    };
    const onSearch = () => {
    };
    const goToDoctorDetail = (id) => {
      common_vendor.index.navigateTo({
        url: `/pages/profile/doctor-detail?id=${id}`
      });
    };
    const contactDoctor = (id) => {
      common_vendor.index.showModal({
        title: "联系医生",
        content: "确定要联系这位医生吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showToast({ title: "联系功能开发中", icon: "none" });
          }
        }
      });
    };
    common_vendor.onMounted(() => {
      getDoctorList();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o([($event) => searchKeyword.value = $event.detail.value, onSearch]),
        b: searchKeyword.value,
        c: common_vendor.f(filteredDoctors.value, (item, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.name),
            b: common_vendor.t(item.title || "主治医师"),
            c: common_vendor.t(item.dept),
            d: item.introduction
          }, item.introduction ? {
            e: common_vendor.t(item.introduction)
          } : {}, {
            f: common_vendor.f(item.tags, (tag, k1, i1) => {
              return {
                a: common_vendor.t(tag),
                b: tag
              };
            }),
            g: common_vendor.o(($event) => contactDoctor(item.id), item.id),
            h: item.id,
            i: common_vendor.o(($event) => goToDoctorDetail(item.id), item.id)
          });
        }),
        d: filteredDoctors.value.length === 0
      }, filteredDoctors.value.length === 0 ? {} : {}, {
        e: common_vendor.o(getDoctorList)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d3f48050"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/profile/personal/mydoctor.js.map
