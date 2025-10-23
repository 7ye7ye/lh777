"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  __name: "hospital",
  setup(__props) {
    const branches = [
      {
        key: "bj",
        label: "主/东校区",
        name: "北京交通大学社区卫生服务中心",
        address: "地址：北京市西直门外上园村3号  电话：010-51682525  邮编：100044  邮箱：xyy2525@126.com",
        intro: "北京交通大学社区卫生服务中心地处海淀区西直门外上园村3号北京交通大学家属区内，隶属于北京交通大学，占地面积1500平方米，建筑总面积4300平方米。设有全科诊室（内科）、外科、眼耳鼻喉科、妇科、健康管理中心、预防保健科、精防科、中医科、口腔科、护理部、药剂科、东校区门诊部和B超心电、化验室、放射科等医技科室。可提供门诊、急诊、家庭病床、计划免疫、儿童保健、慢病管理、老年人健康管理、孕产妇健康管理、家医签约等基本医疗服务和国家基本公共卫生服务。服务人群包含学校教职工、学生、家属及周边群众共计约4万人，年平均门、急诊量17余万人次，每年承担着全校教职工、新生健康筛查工作。\n\n全院职工91人，目前事业编制44人，非事业编制47人。其中卫生技术人员77人，占85%。卫生技术人员中，具备副高级以上职称的卫生技术人员30人，中级职称37人，中级及以上职称占87%。现有全科医师17名，社区护士6名，社区防保医师4名、国家执业药师7名、健康管理师4名。\n\n医院拥有多台进口大型仪器设备，包括彩色多普勒超声仪、经颅多普勒血流分析仪、数字化X光机、骨密度仪、C13检测仪、肺功能仪、动脉硬化检测仪、全自动生化分析仪、全血细胞分析仪、多功能全定量特种蛋白金标检测仪、24小时动态心电监测、24小时动态血压监测、口腔综合治疗台、口腔X线拍片机、眼科综合治疗台、耳鼻喉综合治疗台、眼底照相机、五官短波治疗仪、非接触式眼压仪、便携式呼吸睡眠检测仪、低周波治疗仪、腰椎牵引床、低频电子脉冲治疗仪、短波治疗仪、红光治疗仪、高压电位治疗仪、电脑中频治疗仪、煎药机和包装机等设备。\n\n1996年被海淀区卫生局批准为一级甲等医院，2002年被定为社区卫生服务中心，是北京市基本医疗保险定点医疗机构，定点机构名称：北京交通大学社区卫生服务中心。是经首都精神文明建设委员会验收合格的规范文明服务达标单位，连续多年获评首都公共卫生文明单位，目前与北京大学第三医院为医联体单位，与北京大学人民医院为专科医联体单位，是北京市技术监督局核发批准的计量合格单位，是北京市区级继续医学教育基地。2011年被海淀区卫生局授予“海淀区社区中医药特色示范中心”。2014年9月被评为海淀区“基层中医综合诊区建设单位”。2015年组织成立“卓越健康宣讲组”，完成糖尿病和血脂异常两个主题的首期科普精品课制作。2015年获得海淀区非区属社区卫生服务中心年终绩效考核三个考核项目的全部第一。2017年获得北京市基本医疗卫生制度建设工作先进集体。2018年首次开展“远程医疗”工作。2019年成为全国老年药学联盟成员单位。2021年国家卫健委办公厅、国家中医药局办公室发布了《关于通报表扬2020年“优质服务基层行”活动中表现突出、成效显著机构的通知》，北京交通大学社区卫生服务中心作为北京市唯一的高校医院榜上有名。2022年被确定为“北京市老年友善医疗机构”和“老年健康服务规范化建设医院”。2022年获批北京市妇女保健和儿童保健规范化AA级门诊。2023年获“全国巾帼建功先进集体”称号。连续多年获得海淀区非区属社区卫生服务中心绩效考核前三名。\n\n医院的服务宗旨是：以人的健康为中心，医德至上、患者至上，以优质的服务、合理的收费、良好的环境为原则，体现全方位的社区卫生服务特色、多层次的预防保健特色、分人群的健康教育特色、防治结合的疾病管理特色。面向全校师生员工和社区群众，建设一个有特色、有品牌、有水平的高校社区卫生服务中心。"
      },
      {
        key: "wh",
        label: "威海校区",
        name: "北京交通大学威海校区校医院",
        address: "山东省威海市文登区现代路 69 号（金海路东）。",
        intro: "南海新区医院在校区内设有医务室和药店，文登市中心医院（三甲级）为校区定点医院，校区还与威海市立医院（三甲级）共同开展生理和心理健康教育活动，学生享受与北京校区同样的公费医疗待遇。"
      }
    ];
    const activeIndex = common_vendor.ref(0);
    const currentBranch = common_vendor.computed(() => branches[activeIndex.value]);
    const hospitalName = common_vendor.computed(() => currentBranch.value.name);
    const switchBranch = (idx) => {
      activeIndex.value = idx;
    };
    const goDepartments = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/hospital/departments" });
    };
    const goDoctors = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/hospital/doctors" });
    };
    const goPhysicalExam = () => {
      common_vendor.index.navigateTo({ url: "/subpkg/hospital/department-detail?deptId=43" });
    };
    return (_ctx, _cache) => {
      return {
        a: common_assets._imports_0,
        b: common_assets._imports_1,
        c: common_vendor.t(hospitalName.value),
        d: common_vendor.f(branches, (b, idx, i0) => {
          return {
            a: common_vendor.t(b.label),
            b: b.key,
            c: idx === activeIndex.value ? 1 : "",
            d: common_vendor.o(($event) => switchBranch(idx), b.key)
          };
        }),
        e: common_vendor.t(currentBranch.value.address),
        f: common_assets._imports_2,
        g: common_assets._imports_2,
        h: common_vendor.o(goDepartments),
        i: common_assets._imports_2,
        j: common_vendor.o(goDoctors),
        k: common_assets._imports_2,
        l: common_vendor.o(goPhysicalExam),
        m: common_assets._imports_2,
        n: common_vendor.t(currentBranch.value.intro)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4e3f1300"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/hospital/hospital.js.map
