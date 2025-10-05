package org.jeecg.modules.base.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.constant.enums.ClientTerminalTypeEnum;
import org.jeecg.common.util.BrowserUtils;
import org.jeecg.modules.base.mapper.BaseCommonMapper;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.vo.HosUser;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: common实现类
 * @author: jeecg-boot
 */
@Service
@Slf4j
public class BaseCommonServiceImpl implements BaseCommonService {

    @Resource
    private BaseCommonMapper baseCommonMapper;

    @Override
    public void addLog(LogDTO logDTO) {
        if(oConvertUtils.isEmpty(logDTO.getId())){
            logDTO.setId(String.valueOf(IdWorker.getId()));
        }
        //保存日志（异常捕获处理，防止数据太大存储失败，导致业务失败）JT-238
        try {   
            logDTO.setCreateTime(new Date());
            baseCommonMapper.saveLog(logDTO);
        } catch (Exception e) {
            log.warn(" LogContent length : "+logDTO.getLogContent().length());
            log.warn(e.getMessage());
        }
    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operatetype, HosUser user) {
        addLogInternal(logContent, logType, operatetype, user, null);
    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operatetype, LoginUser user) {
        addLogInternal(logContent, logType, operatetype, null, user);
    }

    private void addLogInternal(String logContent, Integer logType, Integer operatetype, HosUser hosUser, LoginUser loginUser) {
        LogDTO sysLog = new LogDTO();
        sysLog.setId(String.valueOf(IdWorker.getId()));
        //注解上的描述,操作日志内容
        sysLog.setLogContent(logContent);
        sysLog.setLogType(logType);
        sysLog.setOperateType(operatetype);
        try {
            //获取request
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            //设置IP地址
            sysLog.setIp(IpUtils.getIpAddr(request));

            try {
                //设置客户端
                if(BrowserUtils.isDesktop(request)){
                    sysLog.setClientType(ClientTerminalTypeEnum.PC.getKey());
                }else{
                    sysLog.setClientType(ClientTerminalTypeEnum.APP.getKey());
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } catch (Exception e) {
            sysLog.setIp("127.0.0.1");
        }
        //获取登录用户信息
        if(hosUser==null && loginUser==null){
            try {
                Object principal = SecurityUtils.getSubject().getPrincipal();
                if(principal instanceof HosUser){
                    hosUser = (HosUser) principal;
                } else if(principal instanceof LoginUser){
                    loginUser = (LoginUser) principal;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        if(hosUser!=null){
            sysLog.setUserid(hosUser.getUserAccount());
            sysLog.setUsername(hosUser.getUserAccount()); // HosUser没有realname字段，使用userAccount
        } else if(loginUser!=null){
            sysLog.setUserid(loginUser.getUsername());
            sysLog.setUsername(loginUser.getRealname());
        }
        sysLog.setCreateTime(new Date());
        //保存日志（异常捕获处理，防止数据太大存储失败，导致业务失败）JT-238
        try {
            baseCommonMapper.saveLog(sysLog);
        } catch (Exception e) {
            log.warn(" LogContent length : "+sysLog.getLogContent().length());
            log.warn(e.getMessage());
        }
    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operateType) {
        addLogInternal(logContent, logType, operateType, null, null);
    }



}
