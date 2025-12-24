package org.jeecg.modules.hospital.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.config.shiro.IgnoreAuth;
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
import org.jeecg.modules.hospital.common.BaseResponse;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private HosUserService userService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private JeecgBaseConfig jeecgBaseConfig;

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
        int status=userRegisterRequest.getStatus();
        int userType=userRegisterRequest.getUserType();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体信息存在空值");
        }
        System.out.println("接收到用户注册："+userAccount+"<UNK>");

        return userService.userRegister(userAccount,userPassword,checkPassword,userType,status);
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

            // 获取客户端类型（从请求头获取）
            String clientType = request.getHeader("Client-Type");
            System.out.println("登录请求 - Client-Type header: " + clientType);

            if (StringUtils.isBlank(clientType)) {
                clientType = request.getHeader("User-Agent");
                System.out.println("登录请求 - User-Agent header: " + clientType);

                // 根据User-Agent判断客户端类型
                if (StringUtils.isNotBlank(clientType)) {
                    String userAgent = clientType.toLowerCase();
                    if (userAgent.contains("miniprogram") || userAgent.contains("micromessenger")) {
                        clientType = "miniprogram"; // 小程序端
                    } else if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
                        clientType = "mobile"; // 移动端
                    } else if (userAgent.contains("mozilla") || userAgent.contains("chrome") || userAgent.contains("safari") || userAgent.contains("firefox") || userAgent.contains("edge")) {
                        clientType = "web"; // Web管理端
                    } else {
                        clientType = "web"; // 默认为Web端
                    }
                } else {
                    clientType = "web"; // 没有User-Agent时默认为Web端
                }
            }

            System.out.println("登录请求 - 检测到的客户端类型: " + clientType);

            // 获取用户类型
            Integer userType = userLoginResult.getUser().getUserType();
            System.out.println("登录请求 - 用户类型: " + userType + " (1=患者, 2=医生, 3=管理员)");

            // 根据客户端类型和用户类型进行验证
            String validationResult = validateUserTypeForClient(userType, clientType);
            if (validationResult != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HashMap<String, Object>() {{
                    put("code", 403);
                    put("message", validationResult);
                    put("description", getUserTypeDescription(userType)+"请前往小程序端登录");
                }});
            }

            System.out.println(userLoginResult);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("code", 0);
                put("data", userLoginResult);
            }});
        } catch (BusinessException e) {
            // 业务异常：返回更清晰的HTTP状态码，并附带description
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String description = e.getDescription();
            String msg = e.getMessage();
            if (description != null && description.contains("用户名或密码错误")) {
                status = HttpStatus.UNAUTHORIZED;
            }
            final String finalDescription = description;
            final String finalMsg = msg;
            final int statusCode = status.value();
            return ResponseEntity.status(status).body(new HashMap<String, Object>() {{
                put("code", statusCode);
                put("message", finalMsg);
                if (finalDescription != null && !finalDescription.isEmpty()) {
                    put("description", finalDescription);
                }
            }});
        } catch (Exception e) {
            e.printStackTrace();
            // 打印完整堆栈信息到控制台，便于排查问题
            System.err.println("登录异常详情:");
            e.printStackTrace();
            Throwable cause = e;
            while (cause != null && !(cause instanceof BusinessException)) {
                cause = cause.getCause();
            }
            if (cause instanceof BusinessException) {
                BusinessException be = (BusinessException) cause;
                HttpStatus status = HttpStatus.BAD_REQUEST;
                String description = be.getDescription();
                String msg = be.getMessage();
                if (description != null && description.contains("用户名或密码错误")) {
                    status = HttpStatus.UNAUTHORIZED;
                }
                final String finalDescription = description;
                final String finalMsg = msg;
                final int statusCode = status.value();
                return ResponseEntity.status(status).body(new HashMap<String, Object>() {{
                    put("code", statusCode);
                    put("message", finalMsg);
                    if (finalDescription != null && !finalDescription.isEmpty()) {
                        put("description", finalDescription);
                    }
                }});
            }
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

                // 获取客户端类型并进行验证
                String clientType = request.getHeader("Client-Type");
                if (StringUtils.isBlank(clientType)) {
                    clientType = request.getHeader("User-Agent");
                    // 根据User-Agent判断客户端类型
                    if (StringUtils.isNotBlank(clientType)) {
                        String userAgent = clientType.toLowerCase();
                        if (userAgent.contains("miniprogram") || userAgent.contains("micromessenger")) {
                            clientType = "miniprogram"; // 小程序端
                        } else if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
                            clientType = "mobile"; // 移动端
                        } else if (userAgent.contains("mozilla") || userAgent.contains("chrome") || userAgent.contains("safari") || userAgent.contains("firefox") || userAgent.contains("edge")) {
                            clientType = "web"; // Web管理端
                        } else {
                            clientType = "web"; // 默认为Web端
                        }
                    } else {
                        clientType = "web"; // 没有User-Agent时默认为Web端
                    }
                }

                // 验证用户类型是否适合当前客户端
                String validationResult = validateUserTypeForClient(currentUser.getUserType(), clientType);
                if (validationResult != null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HashMap<String, Object>() {{
                        put("code", 403);
                        put("message", validationResult);
                        put("description", getUserTypeDescription(currentUser.getUserType())+"请前往小程序端登录");
                    }});
                }

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

            // 获取客户端类型并进行验证
            String clientType = request.getHeader("Client-Type");
            if (StringUtils.isBlank(clientType)) {
                clientType = request.getHeader("User-Agent");
                // 根据User-Agent判断客户端类型
                if (StringUtils.isNotBlank(clientType)) {
                    String userAgent = clientType.toLowerCase();
                    if (userAgent.contains("miniprogram") || userAgent.contains("micromessenger")) {
                        clientType = "miniprogram"; // 小程序端
                    } else if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
                        clientType = "mobile"; // 移动端
                    } else if (userAgent.contains("mozilla") || userAgent.contains("chrome") || userAgent.contains("safari") || userAgent.contains("firefox") || userAgent.contains("edge")) {
                        clientType = "web"; // Web管理端
                    } else {
                        clientType = "web"; // 默认为Web端
                    }
                } else {
                    clientType = "web"; // 没有User-Agent时默认为Web端
                }
            }

            // 验证用户类型是否适合当前客户端
            String validationResult = validateUserTypeForClient(userVo.getUserType(), clientType);
            if (validationResult != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new HashMap<String, Object>() {{
                    put("code", 403);
                    put("message", validationResult);
                    put("description", getUserTypeDescription(userVo.getUserType())+"请前往小程序端登录");
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

    /**
     * 修改密码（自动获取当前登录用户）
     */
    @PostMapping("/changePassword")
    public BaseResponse<Boolean> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        try {
            if (request == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体不能为空");
            }

            String oldPassword = request.getOldPassword();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();

            if (StringUtils.isAnyBlank(oldPassword, newPassword, confirmPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码参数不能为空");
            }

            // 从Token中获取当前登录用户ID
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录或登录已过期");
            }

            return userService.changePassword(userId, oldPassword, newPassword, confirmPassword);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败：" + e.getMessage());
        }
    }

    /**
     * 从请求中获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            // 1. 优先从Session获取
            Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            if (userObj instanceof HosUser) {
                HosUser user = (HosUser) userObj;
                return user.getUserId();
            }

            // 2. 从Token获取
            String token = request.getHeader("X-Access-Token");
            if (StringUtils.isBlank(token)) {
                String auth = request.getHeader("Authorization");
                if (StringUtils.isNotBlank(auth) && auth.startsWith("Bearer ")) {
                    token = auth.substring(7);
                }
            }

            if (StringUtils.isNotBlank(token)) {
                String username = JwtUtil.getUsername(token);
                if (StringUtils.isNotBlank(username)) {
                    org.jeecg.common.system.vo.HosUser userVo = userService.getHosUserByAccount(username);
                    if (userVo != null) {
                        return userVo.getUserId();
                    }
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证用户类型是否适合当前客户端
     * @param userType 用户类型 (1-患者, 2-医生, 3-管理员)
     * @param clientType 客户端类型 (miniprogram, mobile, web)
     * @return 错误信息，如果验证通过则返回null
     */
    private String validateUserTypeForClient(Integer userType, String clientType) {
        // 患者用户只能在小程序端登录
        if (userType == 1) {
            if ("miniprogram".equals(clientType)) {
                return null; // 允许
            } else {
                return "患者账号只能在小程序端登录";
            }
        }

        // 医生用户只能在小程序端登录
        if (userType == 2) {
            if ("miniprogram".equals(clientType)) {
                return null; // 允许
            } else {
                return "医生账号只能在小程序端登录";
            }
        }

        // 管理员用户只能在Web端登录（管理后台）
        if (userType == 3) {
            if ("web".equals(clientType)) {
                return null; // 允许
            } else {
                return "管理员账号只能在Web管理端登录";
            }
        }

        return "未知用户类型";
    }

    /**
     * 获取用户类型描述
     * @param userType 用户类型
     * @return 用户类型描述
     */
    private String getUserTypeDescription(Integer userType) {
        switch (userType) {
            case 1:
                return "患者";
            case 2:
                return "医生";
            case 3:
                return "管理员";
            default:
                return "未知类型";
        }
    }
}
