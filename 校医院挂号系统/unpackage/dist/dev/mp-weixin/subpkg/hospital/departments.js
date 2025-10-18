"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_department = require("../../api/department.js");
const _sfc_main = {
  __name: "departments",
  setup(__props) {
    const keyword = common_vendor.ref("");
    const departmentTree = common_vendor.ref([]);
    const originalTree = common_vendor.ref([]);
    const isSearchMode = common_vendor.ref(false);
    const loadDepartmentTree = async () => {
      try {
        const res = await api_department.getDepartmentTree();
        common_vendor.index.__f__("log", "at subpkg/hospital/departments.vue:92", "科室树数据:", res);
        let data = res;
        if (res && res.data) {
          data = res.data;
        } else if (res && res.result) {
          data = res.result;
        }
        if (data && Array.isArray(data)) {
          departmentTree.value = data.map((item) => ({
            ...item,
            expanded: false
            // 默认折叠
          }));
          originalTree.value = [...departmentTree.value];
        } else {
          common_vendor.index.__f__("warn", "at subpkg/hospital/departments.vue:109", "科室树数据格式异常:", res);
          common_vendor.index.showToast({
            title: "数据格式异常",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at subpkg/hospital/departments.vue:116", "加载科室树失败:", error);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      }
    };
    const toggleFirstLevel = (index) => {
      departmentTree.value[index].expanded = !departmentTree.value[index].expanded;
    };
    const handleSearch = async () => {
      if (!keyword.value.trim()) {
        departmentTree.value = [...originalTree.value];
        isSearchMode.value = false;
        return;
      }
      try {
        const res = await api_department.searchDepartments(keyword.value);
        common_vendor.index.__f__("log", "at subpkg/hospital/departments.vue:140", "搜索结果:", res);
        let data = res;
        if (res && res.data) {
          data = res.data;
        } else if (res && res.result) {
          data = res.result;
        }
        if (data && Array.isArray(data)) {
          isSearchMode.value = true;
          departmentTree.value = data.map((item) => ({
            deptId: item.deptId,
            deptName: item.deptName,
            deptDesc: item.deptDesc,
            location: item.location,
            expanded: true,
            children: []
          }));
        } else {
          common_vendor.index.__f__("warn", "at subpkg/hospital/departments.vue:162", "搜索结果格式异常:", res);
          common_vendor.index.showToast({
            title: "搜索数据格式异常",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at subpkg/hospital/departments.vue:169", "搜索失败:", error);
        common_vendor.index.showToast({
          title: "搜索失败",
          icon: "none"
        });
      }
    };
    const navigateToDetail = (dept) => {
      common_vendor.index.navigateTo({
        url: `/pages/hospital/department-detail?deptId=${dept.deptId}`
      });
    };
    common_vendor.onMounted(() => {
      loadDepartmentTree();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o([($event) => keyword.value = $event.detail.value, handleSearch]),
        b: keyword.value,
        c: isSearchMode.value
      }, isSearchMode.value ? {
        d: common_vendor.f(departmentTree.value, (item, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(item.deptName),
            b: common_vendor.t(item.deptDesc || "暂无介绍"),
            c: item.location
          }, item.location ? {
            d: common_vendor.t(item.location)
          } : {}, {
            e: index,
            f: common_vendor.o(($event) => navigateToDetail(item), index)
          });
        })
      } : {
        e: common_vendor.f(departmentTree.value, (firstLevel, index, i0) => {
          return common_vendor.e({
            a: common_vendor.t(firstLevel.deptName),
            b: firstLevel.expanded ? 1 : "",
            c: common_vendor.o(($event) => toggleFirstLevel(index), index),
            d: firstLevel.expanded
          }, firstLevel.expanded ? {
            e: common_vendor.f(firstLevel.children, (secondLevel, sIndex, i1) => {
              return common_vendor.e({
                a: common_vendor.t(secondLevel.deptName),
                b: common_vendor.t(secondLevel.deptDesc || "暂无介绍"),
                c: secondLevel.location
              }, secondLevel.location ? {
                d: common_vendor.t(secondLevel.location)
              } : {}, {
                e: sIndex,
                f: common_vendor.o(($event) => navigateToDetail(secondLevel), sIndex)
              });
            })
          } : {}, {
            f: index
          });
        }),
        f: common_assets._imports_0$3
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-df434fb1"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/subpkg/hospital/departments.js.map
