package com.rookie.personal_project.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class FileUtil {
    public static final String ROOT_PATH = "E:\\share";

    public static Set<String> getChild(String rootPath) throws IOException {
        Path path = Paths.get(rootPath);
        Set<Path> childPaths = Files.list(path).collect(Collectors.toSet());
        return childPaths.stream().map(Path::toString).collect(Collectors.toSet());
    }

    public static void main(String[] args) {
        try {
            Set<String> child = getChild(ROOT_PATH);
            System.out.println(child.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
