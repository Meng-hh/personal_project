package com.rookie.personal_project;

import com.rookie.personal_project.config.FileRootConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class PersonalProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalProjectApplication.class, args);
    }

}
