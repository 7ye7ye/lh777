import type { Department } from '/@/api/hospital/department';

export interface DepartmentTreeNode {
  title: string;
  value: number;
  key: number;
  children?: DepartmentTreeNode[];
}

/**
 * 将科室列表转换为树形结构
 * @param departments 科室列表
 * @returns 树形结构的科室数据
 */
export function convertDepartmentsToTree(departments: Department[]): DepartmentTreeNode[] {
  if (!Array.isArray(departments) || departments.length === 0) {
    return [];
  }

  // 创建映射表，方便查找
  const deptMap = new Map<number, DepartmentTreeNode>();
  const rootNodes: DepartmentTreeNode[] = [];

  // 第一遍遍历：创建所有节点
  departments.forEach((dept) => {
    const node: DepartmentTreeNode = {
      title: dept.deptName,
      value: dept.deptId,
      key: dept.deptId,
      children: [],
    };
    deptMap.set(dept.deptId, node);
  });

  // 第二遍遍历：建立父子关系
  departments.forEach((dept) => {
    const node = deptMap.get(dept.deptId);
    if (!node) return;

    if (dept.deptLevel === 1 || !dept.parentDeptId) {
      // 一级科室，添加到根节点
      rootNodes.push(node);
    } else {
      // 二级科室，添加到父节点的children中
      const parentNode = deptMap.get(dept.parentDeptId);
      if (parentNode) {
        if (!parentNode.children) {
          parentNode.children = [];
        }
        parentNode.children.push(node);
      } else {
        // 如果找不到父节点，也作为根节点添加
        rootNodes.push(node);
      }
    }
  });

  // 清理空的children数组
  const cleanNode = (node: DepartmentTreeNode): DepartmentTreeNode => {
    if (node.children && node.children.length === 0) {
      delete node.children;
    } else if (node.children && node.children.length > 0) {
      node.children = node.children.map(cleanNode);
    }
    return node;
  };

  return rootNodes.map(cleanNode);
}

