package org.jeecg.modules.hospital.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Order(1) // 设置高优先级，确保在 CommonController 之前处理
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${jeecg.path.upload}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /sys/common/static/** 映射到 back-end/upload 目录
        // 使用配置的 uploadPath，而不是硬编码路径
        // 路径末尾需要加 /，确保正确映射
        String filePath = "file:" + uploadPath;
        if (!filePath.endsWith("/") && !filePath.endsWith("\\")) {
            filePath = filePath + "/";
        }
        // 设置高优先级，确保在 CommonController 的 /static/** 之前处理
        registry.addResourceHandler("/sys/common/static/**")
                .addResourceLocations(filePath)
                .setCacheControl(org.springframework.http.CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS));

        // 如果项目里已经有别的静态资源映射，可在这里一起配置
    }
}
