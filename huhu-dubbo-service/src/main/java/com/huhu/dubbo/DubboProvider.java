package com.huhu.dubbo;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 启动类
 */
@SpringBootApplication
public class DubboProvider {

    public static void main(String[] args) {
        // 非 Web 应用
        new SpringApplicationBuilder(DubboProvider.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
