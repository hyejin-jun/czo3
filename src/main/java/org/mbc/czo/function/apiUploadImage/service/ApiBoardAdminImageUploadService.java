package org.mbc.czo.function.apiUploadImage.service;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.boardAdmin.domain.BoardAdmin;
import org.mbc.czo.function.boardAdmin.repository.BoardAdminRepository;
import org.mbc.czo.function.apiUploadImage.domain.BoardAdminImages;
import org.mbc.czo.function.apiUploadImage.dto.delete.ImageDeleteRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRes;
import org.mbc.czo.function.apiUploadImage.repository.BoardAdminImageJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiBoardAdminImageUploadService implements ApiImageUploader {

    private final BoardAdminImageJpaRepository boardAdminImageJpaRepository;
    private final BoardAdminRepository boardAdminRepository;

    @Override
    @Transactional
    public ImageUploadRes upload(ImageUploadRef imageUploadRef) throws IOException {
        List<String> savedUrls = new ArrayList<>();
        String tempKey = null;

        for (MultipartFile file : imageUploadRef.getFiles()) {
            if (file.isEmpty()) continue;

            String boardIdStr = imageUploadRef.getExtraData().get("boardId");
            String relativePath = ImageUploadUtils.saveFile(file, "board");
            String originalName = file.getOriginalFilename();
            String storedName = Paths.get(relativePath).getFileName().toString();

            if ("temp".equals(boardIdStr)) {
                tempKey = imageUploadRef.getExtraData().get("tempKey");
                if (tempKey == null || tempKey.isEmpty())
                    throw new IllegalArgumentException("tempKey 없음");

                BoardAdminImages boardImage = new BoardAdminImages(originalName, storedName, relativePath, tempKey, null);
                boardAdminImageJpaRepository.save(boardImage);

            } else {
                Long boardId = Long.parseLong(boardIdStr);
                BoardAdmin board = boardAdminRepository.findById(boardId)
                        .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

                BoardAdminImages boardImage = new BoardAdminImages(originalName, storedName, relativePath, null, board);
                boardAdminImageJpaRepository.save(boardImage);
            }

            savedUrls.add("/uploads/" + relativePath);
        }

        return ImageUploadRes.createImageUploadRes(savedUrls, tempKey);
    }

    @Override
    @Transactional
    public void deleteImage(ImageDeleteRef imageDeleteRef) throws IOException {

        // 1. board 이미지 검색
        Optional<BoardAdminImages> boardImageOpt = boardAdminImageJpaRepository.findByStoredFileName(imageDeleteRef.getStoredFileName());
        if (boardImageOpt.isPresent()) {
            BoardAdminImages image = boardImageOpt.get();
            Files.deleteIfExists(Paths.get(ImageUploadUtils.UPLOAD_ROOT, image.getUploadPath()));
            boardAdminImageJpaRepository.delete(image);
            return;
        }

        // 3. 없으면 예외
        throw new IllegalArgumentException("이미지 없음");
    }
}
