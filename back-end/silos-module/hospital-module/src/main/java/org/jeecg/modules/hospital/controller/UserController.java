package org.jeecg.modules.hospital.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.service.HosUserService;
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

    @IgnoreAuth
    @PostMapping("/register2")
    public long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        System.out.println("已收到请求");
        if(userRegisterRequest==null){
            return 0;
        }
        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return 0;
        }
        return userService.userRegister(userAccount,userPassword,checkPassword);
    }

    @IgnoreAuth
    @PostMapping("/login2")
    public ResponseEntity<?> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        System.out.println("已收到请求");
        if (userLoginRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("请求体不能为空");
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("用户名和用户密码不能为空");
        }
        // 调用服务层进行登录验证
        HosUser user = userService.userLogin(userAccount, userPassword, request);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
        System.out.println(user);
        // 登录成功，返回用户信息（可以根据需要去掉敏感信息）
        return ResponseEntity.ok(user);
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取当前会话
            HttpSession session = request.getSession(false);
            if (session != null) {
                // 使会话失效
                session.invalidate();
            }

            // 清除相关的 cookie
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
                put("code", 0);
                put("message", "退出成功");
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HashMap<String, Object>() {{
                        put("code", 500);
                        put("message", "退出失败");
                    }});
        }
    }
}
