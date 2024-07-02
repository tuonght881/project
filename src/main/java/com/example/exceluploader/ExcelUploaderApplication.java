package com.example.exceluploader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ExcelUploaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelUploaderApplication.class, args);
    }
}
