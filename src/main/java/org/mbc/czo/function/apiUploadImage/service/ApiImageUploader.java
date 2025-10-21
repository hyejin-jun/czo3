package org.mbc.czo.function.apiUploadImage.service;

import org.mbc.czo.function.apiUploadImage.dto.delete.ImageDeleteRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRes;

import java.io.IOException;


public interface ApiImageUploader {


    ImageUploadRes upload(ImageUploadRef imageUploadRef) throws IOException;

    void deleteImage(ImageDeleteRef imageDeleteRef) throws IOException;
}
