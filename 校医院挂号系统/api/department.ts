import { http } from '../utils/request';
import { mockDepartmentTree, mockDepartmentDetail } from './department-mock';

// 是否使用模拟数据（当后端服务器不可用时）
const USE_MOCK_DATA = true;

/**
 * 获取科室树形结构（一级+二级）
 */
export const getDepartmentTree = async () => {
  if (USE_MOCK_DATA) {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 500));
    return mockDepartmentTree;
  }
  
  try {
    return await http.get('/applet/department/tree');
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    return mockDepartmentTree;
  }
};

/**
 * 根据父科室ID获取二级科室
 */
export const getSecondLevelDepartments = async (parentDeptId: number) => {
  if (USE_MOCK_DATA) {
    await new Promise(resolve => setTimeout(resolve, 300));
    const parentDept = mockDepartmentTree.find(dept => dept.deptId === parentDeptId);
    return parentDept ? parentDept.children || [] : [];
  }
  
  try {
    return await http.get('/applet/department/second-level', { parentDeptId });
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    const parentDept = mockDepartmentTree.find(dept => dept.deptId === parentDeptId);
    return parentDept ? parentDept.children || [] : [];
  }
};

/**
 * 搜索科室
 */
export const searchDepartments = async (keyword: string) => {
  if (USE_MOCK_DATA) {
    await new Promise(resolve => setTimeout(resolve, 300));
    const allDepts = [];
    mockDepartmentTree.forEach(parent => {
      allDepts.push(parent);
      if (parent.children) {
        allDepts.push(...parent.children);
      }
    });
    return allDepts.filter(dept => 
      dept.deptName.includes(keyword) || 
      (dept.deptDesc && dept.deptDesc.includes(keyword))
    );
  }
  
  try {
    return await http.get('/applet/department/search', { keyword });
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    const allDepts = [];
    mockDepartmentTree.forEach(parent => {
      allDepts.push(parent);
      if (parent.children) {
        allDepts.push(...parent.children);
      }
    });
    return allDepts.filter(dept => 
      dept.deptName.includes(keyword) || 
      (dept.deptDesc && dept.deptDesc.includes(keyword))
    );
  }
};

/**
 * 获取科室详情
 */
export const getDepartmentDetail = async (deptId: number) => {
  if (USE_MOCK_DATA) {
    await new Promise(resolve => setTimeout(resolve, 300));
    return mockDepartmentDetail;
  }
  
  try {
    return await http.get(`/applet/department/${deptId}`);
  } catch (error) {
    console.warn('后端接口不可用，使用模拟数据:', error);
    return mockDepartmentDetail;
  }
};
