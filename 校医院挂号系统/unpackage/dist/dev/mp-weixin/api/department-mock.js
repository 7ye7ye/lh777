"use strict";
const mockDepartmentTree = [
  {
    deptId: 1,
    deptName: "疫苗预约",
    deptDesc: "疫苗接种服务",
    children: [
      {
        deptId: 11,
        deptName: "老年人免费流感疫苗预约",
        deptDesc: "65岁以上老年人免费流感疫苗接种",
        location: "预防保健科"
      },
      {
        deptId: 12,
        deptName: "儿童接种疫苗预约",
        deptDesc: "儿童常规疫苗接种服务",
        location: "预防保健科"
      },
      {
        deptId: 13,
        deptName: "自费流感疫苗预约",
        deptDesc: "自费流感疫苗接种服务",
        location: "预防保健科"
      },
      {
        deptId: 14,
        deptName: "麻风腮疫苗",
        deptDesc: "麻疹、风疹、腮腺炎联合疫苗",
        location: "预防保健科"
      },
      {
        deptId: 15,
        deptName: "甲肝、乙肝疫苗接种",
        deptDesc: "甲型肝炎、乙型肝炎疫苗接种",
        location: "预防保健科"
      }
    ]
  },
  {
    deptId: 2,
    deptName: "B超室",
    deptDesc: "超声检查服务",
    children: [
      {
        deptId: 21,
        deptName: "超声心动图",
        deptDesc: "心脏超声检查",
        location: "B超室"
      },
      {
        deptId: 22,
        deptName: "肝胆胰脾彩超",
        deptDesc: "腹部器官彩色超声检查",
        location: "B超室"
      },
      {
        deptId: 23,
        deptName: "其他项目彩超",
        deptDesc: "其他部位彩色超声检查",
        location: "B超室"
      }
    ]
  },
  {
    deptId: 3,
    deptName: "公疗报销",
    deptDesc: "公费医疗报销服务",
    children: [
      {
        deptId: 31,
        deptName: "毕业生公疗报销预约",
        deptDesc: "毕业生公费医疗报销服务",
        location: "财务科"
      },
      {
        deptId: 32,
        deptName: "学生专属预约",
        deptDesc: "在校学生公费医疗报销",
        location: "财务科"
      },
      {
        deptId: 33,
        deptName: "教职工专属预约",
        deptDesc: "教职工公费医疗报销",
        location: "财务科"
      }
    ]
  },
  {
    deptId: 4,
    deptName: "体检科",
    deptDesc: "健康体检服务",
    children: [
      {
        deptId: 41,
        deptName: "体检复查预约",
        deptDesc: "体检结果复查服务",
        location: "体检科"
      }
    ]
  },
  {
    deptId: 5,
    deptName: "口腔科",
    deptDesc: "口腔疾病诊疗",
    children: [
      {
        deptId: 51,
        deptName: "口腔咨询门诊",
        deptDesc: "口腔健康咨询和初步检查",
        location: "口腔科"
      },
      {
        deptId: 52,
        deptName: "口腔科",
        deptDesc: "口腔疾病诊疗服务",
        location: "口腔科"
      }
    ]
  },
  {
    deptId: 6,
    deptName: "护理科",
    deptDesc: "护理服务",
    children: [
      {
        deptId: 61,
        deptName: "骨密度检测",
        deptDesc: "骨密度检查服务",
        location: "护理科"
      },
      {
        deptId: 62,
        deptName: "PICC换药",
        deptDesc: "PICC导管维护服务",
        location: "护理科"
      }
    ]
  }
];
const mockDepartmentDetail = {
  deptId: 11,
  deptName: "老年人免费流感疫苗预约",
  deptDesc: "为65岁以上老年人提供免费流感疫苗接种服务。流感疫苗是预防流感最有效的方法，特别对老年人等高风险人群具有重要意义。我们提供专业的疫苗接种服务，确保接种安全有效。",
  location: "预防保健科",
  createTime: "2024-01-01 10:00:00",
  updateTime: "2024-01-01 10:00:00"
};
exports.mockDepartmentDetail = mockDepartmentDetail;
exports.mockDepartmentTree = mockDepartmentTree;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/department-mock.js.map
