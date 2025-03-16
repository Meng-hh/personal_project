package com.rookie.personal_project.domain;

import lombok.Data;

@Data
public class FileInfo {
    private String name;
    private boolean isDirectory;
    private String fullPath;
}
