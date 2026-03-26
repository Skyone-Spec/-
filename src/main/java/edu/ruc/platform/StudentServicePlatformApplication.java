package edu.ruc.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class StudentServicePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentServicePlatformApplication.class, args);
    }
}
