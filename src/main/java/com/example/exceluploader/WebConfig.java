package com.example.exceluploader;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/script.js")  // Đường dẫn URL để truy cập tệp script.js
            .addResourceLocations("classpath:/static/");  // Đường dẫn tới thư mục chứa tệp script.js trong resources
    }
}
