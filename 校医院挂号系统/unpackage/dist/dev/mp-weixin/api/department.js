"use strict";
require("../common/vendor.js");
require("../utils/request.js");
const api_departmentMock = require("./department-mock.js");
const getDepartmentTree = async () => {
  {
    await new Promise((resolve) => setTimeout(resolve, 500));
    return api_departmentMock.mockDepartmentTree;
  }
};
const searchDepartments = async (keyword) => {
  {
    await new Promise((resolve) => setTimeout(resolve, 300));
    const allDepts = [];
    api_departmentMock.mockDepartmentTree.forEach((parent) => {
      allDepts.push(parent);
      if (parent.children) {
        allDepts.push(...parent.children);
      }
    });
    return allDepts.filter(
      (dept) => dept.deptName.includes(keyword) || dept.deptDesc && dept.deptDesc.includes(keyword)
    );
  }
};
const getDepartmentDetail = async (deptId) => {
  {
    await new Promise((resolve) => setTimeout(resolve, 300));
    return api_departmentMock.mockDepartmentDetail;
  }
};
exports.getDepartmentDetail = getDepartmentDetail;
exports.getDepartmentTree = getDepartmentTree;
exports.searchDepartments = searchDepartments;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/department.js.map
