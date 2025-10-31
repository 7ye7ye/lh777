package org.jeecg.modules.hospital.controller.request;

import lombok.Data;
import java.io.Serializable;

/**
 * 患者列表查询请求类（适配前端就诊人列表需求）
 */
@Data // Lombok注解，自动生成getter/setter/toString，无需手动编写
public class PatientListRequest implements Serializable {
    // 新增：用户唯一标识（关联 hos_user 表的 userId，前端必传）
    private Long userId;
    /**
     * 1. 分页基础参数（前端分页组件必备）
     */
    // 当前页码（默认1，前端不传时用默认值）
    private Integer pageNum = 1;
    // 每页条数（默认10，可根据前端列表展示密度调整）
    private Integer pageSize = 10;

    /**
     * 2. 前端筛选需求参数（就诊人列表常用筛选条件）
     */
    // 患者姓名（模糊查询，前端可通过输入框搜索就诊人）
    private String patientName;

    // 患者类型（1-学生；2-教师；3-职工，前端下拉筛选不同身份的就诊人）
    private Integer patientType;

    // 身份认证状态（0-未审核；1-已通过；2-未通过，前端可筛选"已认证"或"未认证"的就诊人）
    private Integer identityVerify;

    /**
     * 3. 扩展筛选参数（基于Patient实体，满足潜在需求）
     */
    // 性别（男/女/未知，前端若需按性别筛选可启用）
    private String gender;

    // 手机号（精确查询，前端若支持"通过手机号找就诊人"可启用）
    private String phone;

    // 学号（学生专属筛选，前端针对学生身份就诊人可启用）
    private String studentId;

    // 工号（教师/职工专属筛选，前端针对教职工身份就诊人可启用）
    private String staffId;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPatientType() {
        return patientType;
    }

    public void setPatientType(Integer patientType) {
        this.patientType = patientType;
    }

    public Integer getIdentityVerify() {
        return identityVerify;
    }

    public void setIdentityVerify(Integer identityVerify) {
        this.identityVerify = identityVerify;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
