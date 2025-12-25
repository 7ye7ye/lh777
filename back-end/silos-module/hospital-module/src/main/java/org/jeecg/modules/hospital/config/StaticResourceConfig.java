package org.jeecg.modules.hospital.config;

import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Order(1) // 设置高优先级，确保在 CommonController 之前处理
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /sys/common/static/** 映射到 back-end/upload 目录
        // 注意：file: 后面是你本机的绝对路径，以 upload 目录结尾
        // 路径末尾需要加 /，确保正确映射
        String uploadPath = "file:D:/yeyeye/课程/大三/专业实训Ⅲ/项目代码/lh777/back-end/upload/";
        if (!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) {
            uploadPath = uploadPath + "/";
        }
        // 设置高优先级，确保在 CommonController 的 /static/** 之前处理
        registry.addResourceHandler("/sys/common/static/**")
                .addResourceLocations(uploadPath)
                .setCacheControl(org.springframework.http.CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS));

        // 如果项目里已经有别的静态资源映射，可在这里一起配置
    }
}
