package com.book.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，让上传的图片可以被访问
        String projectRoot = System.getProperty("user.dir");
        String uploadPath = "file:" + projectRoot + "/uploads/";

        System.out.println("静态资源映射路径: " + uploadPath);

        // 配置静态资源映射，让上传的图片可以被访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
        String path = new File("uploads/").getAbsolutePath();
        System.out.println("静态资源映射的绝对路径: " + path);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}
