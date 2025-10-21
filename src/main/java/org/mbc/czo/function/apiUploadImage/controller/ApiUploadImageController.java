package org.mbc.czo.function.apiUploadImage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.mbc.czo.function.apiUploadImage.dto.delete.ImageDeleteRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRef;
import org.mbc.czo.function.apiUploadImage.service.ApiImageUploader;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRes;
import org.mbc.czo.function.apiUploadImage.service.ImageUploaderFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Log4j2
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ApiUploadImageController {

    private final ImageUploaderFactory imageUploaderFactory;


    //RestAPI  REST 스타일은 URL이 행동이 아닌 자원을 표현, HTTP 상태 코드로 성공/실패를 전달
    //json 활용한 데이터전달
    //json전달 형태
//    {
//        "files": ["url1.jpg", "url2.png"],
//        "message": "업로드 및 DB 저장 성공"
//    }
    //static/js/uploadImages.js 참고

    @PostMapping("/image")
    public ApiResult<ImageUploadRes> uploadImage(@ModelAttribute ImageUploadRef imageUploadRef) {

        try {
            ApiImageUploader uploader = imageUploaderFactory.getUploader(imageUploadRef.getExtraData());
            ImageUploadRes imageUploadRes = uploader.upload(imageUploadRef);
            log.info("uploadImage imageUploadRes:" + imageUploadRes);
            return ApiResult.created(imageUploadRes);

        }catch (IllegalArgumentException | IOException e)  {

            return ApiResult.fail(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ApiResult<ImageDeleteRef> deleteImage(@ModelAttribute ImageDeleteRef imageDeleteRef) {

        try {
            ApiImageUploader uploader = imageUploaderFactory.getUploader(imageDeleteRef.getExtraData());
            uploader.deleteImage(imageDeleteRef);
            return ApiResult.none();

        } catch (IllegalArgumentException | IOException e)  {

            return ApiResult.fail(e.getMessage());
        }
    }
}
