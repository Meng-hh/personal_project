package com.rookie.personal_project.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DownloadService {

    public String getChild(String rootPath) {
        Path path = Paths.get(rootPath);
        File[] files = path.toFile().listFiles();

        return null;
    }
}
