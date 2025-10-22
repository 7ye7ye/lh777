package org.jeecg.config.shiro.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.shiro.JwtToken;
import org.jeecg.config.shiro.ignore.InMemoryIgnoreAuth;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Description: 鉴权登录拦截器
 * @Author: Scott
 * @Date: 2018/10/7
 **/
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * 默认开启跨域设置（使用单体）
     * 微服务情况下，此属性设置为false
     */
    private boolean allowOrigin = true;

    public JwtFilter(){}
    public JwtFilter(boolean allowOrigin){
        this.allowOrigin = allowOrigin;
    }

    /**
     * 执行登录认证
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            // 判断当前路径是不是注解了@IngoreAuth路径，如果是，则放开验证
            if (InMemoryIgnoreAuth.contains(((HttpServletRequest) request).getServletPath())) {
                return true;
            }

            executeLogin(request, response);
            return true;
        } catch (Exception e) {
            JwtUtil.responseError(response,401,CommonConstant.TOKEN_IS_INVALID_MSG);
            return false;
            //throw new AuthenticationException("Token失效，请重新登录", e);
        }
    }

    /**
     * 【核心修改】补充Bearer Token提取逻辑，兼容原有X-ACCESS-TOKEN和token参数方式
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = null;

        // 1. 优先从原有X-ACCESS-TOKEN头获取Token（保留原有逻辑）
        token = httpServletRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);

        // 2. 原有逻辑：若X-ACCESS-TOKEN为空，从token参数获取（保留原有逻辑）
        if (oConvertUtils.isEmpty(token)) {
            token = httpServletRequest.getParameter("token");
        }

        // 【新增逻辑1】3. 若上述两种方式都为空，从Authorization头提取Bearer Token
        if (oConvertUtils.isEmpty(token)) {
            String authHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            // 判断是否为Bearer Token格式（前缀为"Bearer "）
            if (oConvertUtils.isNotEmpty(authHeader) && authHeader.startsWith("Bearer ")) {
                // 提取"Bearer "后面的纯Token字符串
                token = authHeader.substring("Bearer ".length()).trim();
                log.debug("从Authorization头提取Bearer Token：{}", token);
            }
        }

        // 【新增逻辑2】Token仍为空时，明确抛出Token为空异常（便于排查）
        if (oConvertUtils.isEmpty(token)) {
            log.info("————————身份认证失败——————————IP地址:  "+ oConvertUtils.getIpAddrByRequest(httpServletRequest) +"，URL:"+httpServletRequest.getRequestURI());
            throw new Exception("token为空!");
        }

        // 原有逻辑：封装为JwtToken并提交认证（不修改）
        JwtToken jwtToken = new JwtToken(token);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        getSubject(request, response).login(jwtToken);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }

    /**
     * 对跨域提供支持（原有逻辑不修改）
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if(allowOrigin){
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, httpServletRequest.getHeader(HttpHeaders.ORIGIN));
            // 允许客户端请求方法
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,OPTIONS,PUT,DELETE");
            // 允许客户端提交的Header
            String requestHeaders = httpServletRequest.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            if (StringUtils.isNotEmpty(requestHeaders)) {
                httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
            }
            // 允许客户端携带凭证信息(是否允许发送Cookie)
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(httpServletRequest.getMethod())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        //update-begin-author:taoyan date:20200708 for:多租户用到
        String tenantId = httpServletRequest.getHeader(CommonConstant.TENANT_ID);
        TenantContext.setTenant(tenantId);
        //update-end-author:taoyan date:20200708 for:多租户用到

        return super.preHandle(request, response);
    }

    /**
     * JwtFilter中ThreadLocal需要及时清除 #3634（原有逻辑不修改）
     *
     * @param request
     * @param response
     * @param exception
     * @throws Exception
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
        //log.info("------清空线程中多租户的ID={}------",TenantContext.getTenant());
        TenantContext.clear();
    }
}
