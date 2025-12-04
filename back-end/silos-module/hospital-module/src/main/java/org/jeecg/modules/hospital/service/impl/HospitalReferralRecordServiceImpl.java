package org.jeecg.modules.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.hospital.entity.HospitalReferralRecord;
import org.jeecg.modules.hospital.mapper.HospitalReferralRecordMapper;
import org.jeecg.modules.hospital.service.HospitalReferralRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * 转诊记录服务实现类
 */
@Service
@Transactional(readOnly = true)
public class HospitalReferralRecordServiceImpl extends ServiceImpl<HospitalReferralRecordMapper, HospitalReferralRecord> implements HospitalReferralRecordService {

    @Autowired
    private HospitalReferralRecordMapper hospitalReferralRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createReferralRecord(HospitalReferralRecord record) {
        try {
            // 设置默认值
            if (record.getReferralStatus() == null) {
                record.setReferralStatus("PENDING");
            }
            if (record.getFollowUpStatus() == null) {
                record.setFollowUpStatus("NONE");
            }
            if (record.getCreateTime() == null) {
                record.setCreateTime(LocalDateTime.now());
            }
            record.setUpdateTime(LocalDateTime.now());

            // 检查是否已存在相同转诊申请ID的记录
            HospitalReferralRecord existingRecord = hospitalReferralRecordMapper.selectByReferralId(record.getReferralId());
            if (existingRecord != null) {
                return Result.error("该转诊申请已存在对应的记录");
            }

            // 保存记录
            hospitalReferralRecordMapper.insert(record);
            return Result.OK("转诊记录创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("转诊记录创建失败：" + e.getMessage());
        }
    }

    @Override
    public Result<HospitalReferralRecord> getByReferralId(Long referralId) {
        try {
            HospitalReferralRecord record = hospitalReferralRecordMapper.selectByReferralId(referralId);
            if (record == null) {
                return Result.error("未找到对应的转诊记录");
            }
            return Result.OK(record);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result<HospitalReferralRecord> getByReferralCode(String referralCode) {
        try {
            HospitalReferralRecord record = hospitalReferralRecordMapper.selectByReferralCode(referralCode);
            if (record == null) {
                return Result.error("未找到对应的转诊记录");
            }
            return Result.OK(record);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateReferralStatus(Long id, String status) {
        try {
            HospitalReferralRecord record = hospitalReferralRecordMapper.selectById(id);
            if (record == null) {
                return Result.error("转诊记录不存在");
            }

            // 更新状态
            record.setReferralStatus(status);
            record.setUpdateTime(LocalDateTime.now());

            // 根据状态设置相应的时间戳
            switch (status) {
                case "SENT":
                    record.setTransferOutTime(LocalDateTime.now());
                    break;
                case "RECEIVED":
                    record.setTransferInTime(LocalDateTime.now());
                    break;
                case "COMPLETED":
                case "FOLLOWED_UP":
                    record.setCompletionTime(LocalDateTime.now());
                    break;
            }

            hospitalReferralRecordMapper.updateById(record);
            return Result.OK("状态更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateFollowUpInfo(Long id, Map<String, Object> followUpInfo) {
        try {
            HospitalReferralRecord record = hospitalReferralRecordMapper.selectById(id);
            if (record == null) {
                return Result.error("转诊记录不存在");
            }

            // 更新随访信息
            if (followUpInfo.containsKey("followUpStatus")) {
                record.setFollowUpStatus((String) followUpInfo.get("followUpStatus"));
            }
            if (followUpInfo.containsKey("followUpDate")) {
                Object followUpDate = followUpInfo.get("followUpDate");
                if (followUpDate instanceof LocalDate) {
                    record.setFollowUpDate((LocalDate) followUpDate);
                } else if (followUpDate instanceof LocalDateTime) {
                    record.setFollowUpDate(((LocalDateTime) followUpDate).toLocalDate());
                } else if (followUpDate instanceof java.util.Date) {
                    record.setFollowUpDate(((java.util.Date) followUpDate)
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
                } else if (followUpDate instanceof String) {
                    String text = ((String) followUpDate).trim();
                    if (!text.isEmpty()) {
                        try {
                            record.setFollowUpDate(LocalDate.parse(text));
                        } catch (Exception parseEx) {
                            // ignore invalid date string, keep existing value
                        }
                    }
                }
            }
            if (followUpInfo.containsKey("followUpDoctor")) {
                record.setFollowUpDoctor((String) followUpInfo.get("followUpDoctor"));
            }
            if (followUpInfo.containsKey("followUpNotes")) {
                record.setFollowUpNotes((String) followUpInfo.get("followUpNotes"));
            }
            if (followUpInfo.containsKey("recoveryStatus")) {
                record.setRecoveryStatus((String) followUpInfo.get("recoveryStatus"));
            }
            if (followUpInfo.containsKey("feedbackRating")) {
                record.setFeedbackRating((Integer) followUpInfo.get("feedbackRating"));
            }
            if (followUpInfo.containsKey("feedbackComments")) {
                record.setFeedbackComments((String) followUpInfo.get("feedbackComments"));
            }

            record.setUpdateTime(LocalDateTime.now());
            hospitalReferralRecordMapper.updateById(record);
            return Result.OK("随访信息更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("随访信息更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getReferralRecordList(Map<String, Object> params) {
        try {
            // 分页参数处理
            int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
            int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "10").toString());
            int startIndex = (pageNum - 1) * pageSize;

            params.put("startIndex", startIndex);
            params.put("pageSize", pageSize);

            // 查询数据
            List<Map<String, Object>> records = hospitalReferralRecordMapper.selectReferralRecordList(params);
            long total = hospitalReferralRecordMapper.selectReferralRecordCount(params);

            Map<String, Object> result = Map.of(
                "records", records,
                "total", total,
                "pageNum", pageNum,
                "pageSize", pageSize
            );

            return Result.OK(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchUpdateStatus(List<Long> ids, String status) {
        try {
            int count = hospitalReferralRecordMapper.batchUpdateStatus(ids, status);
            return Result.OK("成功更新 " + count + " 条记录的状态");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量更新失败：" + e.getMessage());
        }
    }
}