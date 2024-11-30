 package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class DemoApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        //WAR 배포 시 호출
        return application.sources(DemoApplication.class);
        // 애플리케이션의 소스를 DemoApplication 클래스로 지정
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
