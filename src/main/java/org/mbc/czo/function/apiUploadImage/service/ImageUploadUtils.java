package org.mbc.czo.function.apiUploadImage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ImageUploadUtils {

    public static final String UPLOAD_ROOT = "C:/image";
    //public static final String UPLOAD_ROOT = "/app/image";

    public static String generateUUIDFileName(String originalName) {
        String extension = originalName.substring(originalName.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    public static Path getFolderPath(String subDir) {
        Path folderPath = Paths.get(UPLOAD_ROOT, subDir);
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectories(folderPath);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 실패: " + folderPath, e);
            }
        }
        return folderPath;
    }

    public static String saveFile(MultipartFile file, String subDir) throws IOException {
        String originalName = file.getOriginalFilename();
        String storedName = generateUUIDFileName(originalName);
        Path folderPath = getFolderPath(subDir);
        Path destination = folderPath.resolve(storedName);
        file.transferTo(destination.toFile());
        return subDir + "/" + storedName;
    }

    public static void deleteFile(String relativePath) throws IOException {
        Path path = Paths.get(UPLOAD_ROOT, relativePath);
        Files.deleteIfExists(path);
    }
}
