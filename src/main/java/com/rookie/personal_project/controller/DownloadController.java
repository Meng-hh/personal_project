package com.rookie.personal_project.controller;

import com.rookie.personal_project.config.FileRootConfig;
import com.rookie.personal_project.domain.FileInfo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class DownloadController {

    FileRootConfig rootConfig;

    @Autowired
    public void setRootConfig(FileRootConfig rootConfig) {
        this.rootConfig = rootConfig;
    }

    @Data
    static class DirectoryResponse {
        private List<FileInfo> directories = new ArrayList<>();
        private List<FileInfo> files = new ArrayList<>();
    }

    @GetMapping("/showRoot")
    public String listFiles(
            @RequestParam(defaultValue = "") String path,
            Model model) {
        model.addAttribute("roots", rootConfig.getRoots());
        return "showRoot";
    }

    @GetMapping("/directory")
    public String getDirectory(
            @RequestParam String path
            , Model model
    ) {
        // 使用URL解码
        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        // 转换路径分隔符（可选）
        decodedPath = decodedPath.replace("/", "\\");

        Path basePath = Paths.get(decodedPath);
        DirectoryResponse response = new DirectoryResponse();

        try (Stream<Path> paths = Files.list(basePath)) {
            paths.forEach(p -> {
                FileInfo info = new FileInfo();
                info.setName(p.getFileName().toString());
                info.setDirectory(Files.isDirectory(p));
                String fullPath = basePath + "\\" + info.getName();
                info.setFullPath(fullPath);

                if (info.isDirectory()) {
                    response.getDirectories().add(info);
                } else {
                    response.getFiles().add(info);
                }
            });
            model.addAttribute("directories", response.directories);
            model.addAttribute("files", response.files);
            return "fileSelector";
        } catch (IOException e) {
            return "error";
        }
    }

    @GetMapping("/download/file")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String path) throws IOException {

        Path targetPath = Paths.get(URLDecoder.decode(path, StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        if (Files.isDirectory(targetPath)) {
            // 处理目录下载（需要实现打包逻辑）
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" +URLEncoder.encode(targetPath.getFileName().toString(), StandardCharsets.UTF_8)  + ".zip\"");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipDirectory(targetPath));
        } else {
            // 处理文件下载
            Resource resource = new FileSystemResource(targetPath);
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8) + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        }
    }

    // 目录打包方法示例
    private Resource zipDirectory(Path dir) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Files.walk(dir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry entry = new ZipEntry(dir.relativize(path).toString());
                        try {
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        byte[] zipBytes = baos.toByteArray();
        return new ByteArrayResource(zipBytes);
    }
}
