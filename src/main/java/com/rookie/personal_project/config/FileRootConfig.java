package com.rookie.personal_project.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "file")
@Data
public class FileRootConfig {

    private List<RootDirectory> roots = new ArrayList<>();

    @Data
    public static class RootDirectory {
        private String id;
        private String name;
        private String path;
        private String icon;
    }
}
