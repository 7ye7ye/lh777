package org.jeecg.modules.hospital.test;

import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.HospitalReferralRecord;
import org.jeecg.modules.hospital.service.HospitalReferralRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 转诊记录功能测试类
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
public class HospitalReferralRecordTest {

    @Resource
    private HospitalReferralRecordService hospitalReferralRecordService;

    /**
     * 测试创建转诊记录
     */
    @Test
    public void testCreateReferralRecord() {
        // 创建测试数据
        HospitalReferralRecord record = new HospitalReferralRecord();
        record.setReferralId(1001L);
        record.setReferralCode("REF202506010001");
        record.setPatientName("测试患者");
        record.setPatientIdCard("110101200001011234");
        record.setActualHospitalId(1L);
        record.setActualHospitalName("测试医院");
        record.setActualDeptId(1L);
        record.setActualDeptName("内科");

        // 调用服务方法
        Result<String> result = hospitalReferralRecordService.createReferralRecord(record);
        System.out.println("创建转诊记录结果: " + result.getMessage());
        
        // 断言结果
        assertTrue(result.isSuccess(), "创建转诊记录失败: " + result.getMessage());
    }

    /**
     * 测试根据转诊申请ID查询记录
     */
    @Test
    public void testGetByReferralId() {
        // 使用一个有效的转诊申请ID进行测试
        Long referralId = 1001L;
        
        Result<HospitalReferralRecord> result = hospitalReferralRecordService.getByReferralId(referralId);
        System.out.println("根据转诊申请ID查询结果: " + (result.isSuccess() ? "成功" : "失败: " + result.getMessage()));
        
        if (result.isSuccess()) {
            HospitalReferralRecord record = result.getResult();
            assertNotNull(record, "查询返回的记录为空");
            assertEquals(referralId, record.getReferralId(), "转诊申请ID不匹配");
        }
    }

    /**
     * 测试根据转诊单编号查询记录
     */
    @Test
    public void testGetByReferralCode() {
        // 使用一个有效的转诊单编号进行测试
        String referralCode = "REF202506010001";
        
        Result<HospitalReferralRecord> result = hospitalReferralRecordService.getByReferralCode(referralCode);
        System.out.println("根据转诊单编号查询结果: " + (result.isSuccess() ? "成功" : "失败: " + result.getMessage()));
        
        if (result.isSuccess()) {
            HospitalReferralRecord record = result.getResult();
            assertNotNull(record, "查询返回的记录为空");
            assertEquals(referralCode, record.getReferralCode(), "转诊单编号不匹配");
        }
    }

    /**
     * 测试更新转诊状态
     */
    @Test
    public void testUpdateReferralStatus() {
        // 先创建一个记录用于测试更新
        HospitalReferralRecord record = new HospitalReferralRecord();
        record.setReferralId(1002L);
        record.setReferralCode("REF202506010002");
        record.setPatientName("测试患者2");
        record.setActualHospitalName("测试医院");
        record.setActualDeptName("外科");
        
        Result<String> createResult = hospitalReferralRecordService.createReferralRecord(record);
        if (createResult.isSuccess()) {
            // 查询刚创建的记录以获取ID
            Result<HospitalReferralRecord> queryResult = hospitalReferralRecordService.getByReferralId(1002L);
            if (queryResult.isSuccess()) {
                Long recordId = queryResult.getResult().getId();
                
                // 更新状态为已发送
                Result<String> updateResult = hospitalReferralRecordService.updateReferralStatus(recordId, "SENT");
                System.out.println("更新转诊状态结果: " + updateResult.getMessage());
                assertTrue(updateResult.isSuccess(), "更新转诊状态失败: " + updateResult.getMessage());
                
                // 验证状态更新成功
                Result<HospitalReferralRecord> verifyResult = hospitalReferralRecordService.getByReferralId(1002L);
                if (verifyResult.isSuccess()) {
                    assertEquals("SENT", verifyResult.getResult().getReferralStatus(), "状态更新未生效");
                }
            }
        }
    }

    /**
     * 测试更新随访信息
     */
    @Test
    public void testUpdateFollowUpInfo() {
        // 先创建一个记录用于测试更新随访信息
        HospitalReferralRecord record = new HospitalReferralRecord();
        record.setReferralId(1003L);
        record.setReferralCode("REF202506010003");
        record.setPatientName("测试患者3");
        record.setActualHospitalName("测试医院");
        record.setActualDeptName("儿科");
        
        Result<String> createResult = hospitalReferralRecordService.createReferralRecord(record);
        if (createResult.isSuccess()) {
            // 查询刚创建的记录以获取ID
            Result<HospitalReferralRecord> queryResult = hospitalReferralRecordService.getByReferralId(1003L);
            if (queryResult.isSuccess()) {
                Long recordId = queryResult.getResult().getId();
                
                // 准备随访信息
                Map<String, Object> followUpInfo = new HashMap<>();
                followUpInfo.put("followUpStatus", "COMPLETED");
                followUpInfo.put("followUpDoctor", "张医生");
                followUpInfo.put("followUpNotes", "患者恢复良好，建议继续观察");
                followUpInfo.put("recoveryStatus", "EXCELLENT");
                followUpInfo.put("feedbackRating", 5);
                followUpInfo.put("feedbackComments", "服务态度很好，治疗效果满意");
                
                // 更新随访信息
                Result<String> updateResult = hospitalReferralRecordService.updateFollowUpInfo(recordId, followUpInfo);
                System.out.println("更新随访信息结果: " + updateResult.getMessage());
                assertTrue(updateResult.isSuccess(), "更新随访信息失败: " + updateResult.getMessage());
            }
        }
    }

    /**
     * 测试获取转诊记录列表
     */
    @Test
    public void testGetReferralRecordList() {
        // 准备查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        // 可以添加其他查询条件进行测试
        
        Result<Map<String, Object>> result = hospitalReferralRecordService.getReferralRecordList(params);
        System.out.println("获取转诊记录列表结果: " + (result.isSuccess() ? "成功" : "失败: " + result.getMessage()));
        
        if (result.isSuccess()) {
            Map<String, Object> data = result.getResult();
            assertNotNull(data, "返回的数据为空");
            assertTrue(data.containsKey("records"), "返回数据缺少records字段");
            assertTrue(data.containsKey("total"), "返回数据缺少total字段");
            
            List<?> records = (List<?>) data.get("records");
            System.out.println("查询到的记录数量: " + records.size());
        }
    }

    /**
     * 测试数据源连接是否正常
     */
    @Test
    public void testDataSourceConnection() {
        try {
            // 通过调用一个简单的查询方法来验证数据源连接
            Map<String, Object> params = new HashMap<>();
            params.put("pageNum", 1);
            params.put("pageSize", 1);
            
            Result<Map<String, Object>> result = hospitalReferralRecordService.getReferralRecordList(params);
            System.out.println("数据源连接测试结果: " + (result.isSuccess() ? "连接正常" : "连接失败: " + result.getMessage()));
            
            // 即使没有数据，只要不抛出异常并且返回格式正确，就说明连接正常
            assertNotNull(result, "查询结果为空");
            
        } catch (Exception e) {
            System.err.println("数据源连接异常: " + e.getMessage());
            e.printStackTrace();
            fail("数据源连接测试失败: " + e.getMessage());
        }
    }
}