package org.jeecg.modules.hospital.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.hospital.common.BaseResponse;
import org.jeecg.modules.hospital.common.ErrorCode;
import org.jeecg.modules.hospital.dto.HosUserLoginResult;
import org.jeecg.modules.hospital.entity.HosUser;
import org.jeecg.modules.hospital.exception.BusinessException;
import org.jeecg.modules.hospital.service.HosUserService;
import org.jeecg.common.system.util.JwtUtil;
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
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        System.out.println("已收到请求");
        if(userRegisterRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体为空");
        }
        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体信息存在空值");
        }
        System.out.println("接收到用户注册："+userAccount+"<UNK>");

        return userService.userRegister(userAccount,userPassword,checkPassword);
    }

    @IgnoreAuth
    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        System.out.println("已收到请求");
        if (userLoginRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                put("code", 400);
                put("message", "请求体不能为空");
            }});
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, Object>() {{
                put("code", 400);
                put("message", "用户名和用户密码不能为空");
            }});
        }

        try {
            HosUserLoginResult userLoginResult = userService.userLogin(userAccount, userPassword, request);
            if (userLoginResult == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<String, Object>() {{
                    put("code", 401);
                    put("message", "用户名或密码错误");
                }});
            }
            System.out.println(userLoginResult);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 0);
                put("data", userLoginResult);
            }});
        } catch (Exception e) {
            e.printStackTrace();
            // 打印完整堆栈信息到控制台，便于排查问题
            System.err.println("登录异常详情:");
            e.printStackTrace();
            // 构建错误信息（需要在匿名内部类外部构建，并声明为final）
            final String errorMessage = (e.getMessage() != null && !e.getMessage().isEmpty())
                ? "登录失败: " + e.getMessage()
                : "登录失败";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", errorMessage);
            }});
        }
    }


    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            System.out.println("Retrieved user info: " + userObj);
            System.out.println("Session ID (get): " + request.getSession().getId());

            // 1) 优先使用会话态
            if (userObj instanceof HosUser) {
                HosUser currentUser = (HosUser) userObj;
                return ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("code", 0);
                    put("data", currentUser.getUserAccount());
                }});
            }

            // 2) 会话不存在时，使用 Token 兜底（X-Access-Token 或 Authorization: Bearer ...）
            String token = request.getHeader("X-Access-Token");
            if (StringUtils.isBlank(token)) {
                String auth = request.getHeader("Authorization");
                if (StringUtils.isNotBlank(auth) && auth.startsWith("Bearer ")) {
                    token = auth.substring(7);
                }
            }

            // 打印请求头信息，便于调试
            System.out.println("请求头 X-Access-Token: " + request.getHeader("X-Access-Token"));
            System.out.println("请求头 Authorization: " + request.getHeader("Authorization"));
            System.out.println("提取的 Token: " + token);

            if (StringUtils.isBlank(token)) {
                System.out.println("Token为空，返回401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<String, Object>() {{
                    put("code", 401);
                    put("message", "未登录或Token缺失");
                }});
            }

            // 3) 从 Token 解析用户名，并查询用户
            String username = JwtUtil.getUsername(token);
            System.out.println("从Token解析的用户名: " + username);

            if (StringUtils.isBlank(username)) {
                System.out.println("Token解析失败，返回401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<String, Object>() {{
                    put("code", 401);
                    put("message", "Token无效或已过期");
                }});
            }

            org.jeecg.common.system.vo.HosUser userVo = userService.getHosUserByAccount(username);
            if (userVo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<String, Object>() {{
                    put("code", 401);
                    put("message", "用户不存在");
                }});
            }

            // 成功返回账号（也可返回完整用户信息）
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 0);
                put("data", userVo.getUserAccount());
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, Object>() {{
                put("code", 500);
                put("message", "获取当前用户失败");
            }});
        }
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
