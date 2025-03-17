package com.rookie.personal_project.controller;

import com.rookie.personal_project.config.FileRootConfig;
import com.rookie.personal_project.domain.FileInfo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
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
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @RequestParam String path) throws IOException {

        Path targetPath = Paths.get(URLDecoder.decode(path, StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        if (Files.isDirectory(targetPath)) {
            // 处理目录下载（需要实现打包逻辑）
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\""
                            + URLEncoder.encode(targetPath.getFileName().toString(),
                            StandardCharsets.UTF_8) + ".zip\"");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream -> {
                        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                            streamAddToZip(zos, targetPath, targetPath);
                        }
                    });
        } else {
            // 处理文件下载
            StreamingResponseBody responseBody = outputStream -> {
                try (InputStream inputStream = Files.newInputStream(targetPath)) {
                    byte[] buffer = new byte[1024 * 1024 * 100];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        outputStream.flush();
                    }
                }
            };
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(targetPath.getFileName().toString(), StandardCharsets.UTF_8) + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseBody);
        }
    }

    // 目录打包方法示例
    private void streamAddToZip(ZipOutputStream zos, Path rootDir, Path currentDir)
            throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    streamAddToZip(zos, rootDir, path); // 递归处理子目录
                } else {
                    addFileToZip(zos, rootDir, path);
                }
            }
        }
    }

    private void addFileToZip(ZipOutputStream zos, Path rootDir, Path file)
            throws IOException {
        String entryName = rootDir.relativize(file).toString().replace('\\', '/');
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[1024 * 1024 * 100]; // 100MB缓冲区
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
                zos.flush();  // 关键：确保分块输出
            }
        }
        zos.closeEntry();
    }
}
