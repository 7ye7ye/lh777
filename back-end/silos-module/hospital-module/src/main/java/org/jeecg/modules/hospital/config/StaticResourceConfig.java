package org.jeecg.modules.hospital.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /jeecg-boot/sys/common/static/** 映射到 back-end/upload 目录
        // 注意：file: 后面是你本机的绝对路径，以 upload 目录结尾
        registry.addResourceHandler("/sys/common/static/**")
                .addResourceLocations("file:D:/yeyeye/课程/大三/专业实训Ⅲ/项目代码/lh777/back-end/upload/");

        // 如果项目里已经有别的静态资源映射，可在这里一起配置
    }
}
