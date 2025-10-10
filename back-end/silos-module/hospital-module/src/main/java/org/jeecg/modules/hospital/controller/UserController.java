package org.jeecg.modules.hospital.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.Md5Util;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.hospital.dto.HosUserLoginResult;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.service.HosUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import static org.jeecg.modules.hospital.contant.UserContant.USER_LOGIN_STATE;
import org.jeecg.modules.hospital.controller.request.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private HosUserService userService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;

    @IgnoreAuth
    @PostMapping("/register")
    public ResponseEntity<?> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请求体不能为空");
        }
        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();
        String userType=userRegisterRequest.getUserType();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("用户名和用户密码不能为空");
        }
        System.out.println("接收到用户注册："+userAccount+"<UNK>");

        return ResponseEntity.ok(userService.userRegister(userAccount,userPassword,checkPassword,userType));
    }

    @IgnoreAuth
    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请求体不能为空");
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        String captcha = userLoginRequest.getCaptcha();
        String checkKey = userLoginRequest.getCheckKey();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("用户名和用户密码不能为空");
        }
        
        // 验证码校验
        if (captcha == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("验证码无效");
        }
        
        String lowerCaseCaptcha = captcha.toLowerCase();
        // 加入密钥作为混淆，避免简单的拼接，被外部利用
        String keyPrefix = Md5Util.md5Encode(checkKey + jeecgBaseConfig.getSignatureSecret(), "utf-8");
        String realKey = keyPrefix + lowerCaseCaptcha;
        Object checkCode = redisUtil.get(realKey);
        
        // 验证码错误
        if (checkCode == null || !checkCode.toString().equals(lowerCaseCaptcha)) {
            System.out.println("验证码错误，key= " + checkKey + ", Ui checkCode= " + lowerCaseCaptcha + ", Redis checkCode = " + checkCode);
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("验证码错误");
        }
        
        // 调用服务层进行登录验证
        HosUserLoginResult userLoginResult = userService.userLogin(userAccount, userPassword, request);

        if (userLoginResult == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
        
        // 登录成功删除验证码
        redisUtil.del(realKey);
        
        System.out.println("用户登录成功: " + userLoginResult);
        // 登录成功，返回用户信息
        return ResponseEntity.ok(userLoginResult);
    }


    @GetMapping("/current")
    public String getCurrentUser(HttpServletRequest request) {
        Object userObj=null;
        try {
            userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            System.out.println("Retrieved user info: " + userObj);
            System.out.println("Session ID (get): " + request.getSession().getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
        HosUser currentUser = (HosUser) userObj;
        if(currentUser==null){
            return null;
        }
        return currentUser.getUserAccount();
    }

    @IgnoreAuth
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 从Header获取token
            String token = request.getHeader("X-Access-Token");
            if (StringUtils.isNotBlank(token)) {
                // 2. 清除Redis中的token缓存
                redisUtil.del("sys:cache:token:" + token);
                System.out.println("清除token缓存: " + token);
            }
            
            // 3. 获取当前会话并使其失效
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // 4. 清除相关的 cookie
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        cookie.setValue("");
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            }

            return ResponseEntity.ok().body(new HashMap<String, Object>() {{
                put("success", true);
                put("message", "退出成功");
                put("code", 200);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, Object>() {{
                        put("success", false);
                        put("message", "退出失败: " + e.getMessage());
                        put("code", 500);
                    }});
        }
    }
}
