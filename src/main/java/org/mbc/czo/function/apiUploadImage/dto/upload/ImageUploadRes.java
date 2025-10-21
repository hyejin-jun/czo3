package org.mbc.czo.function.apiUploadImage.dto.upload;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadRes {

    private List<String> urls;
    private String tempKey;

    public static ImageUploadRes createImageUploadRes(List<String> urlsParam, String tempKeyParam) {
        return  ImageUploadRes.builder()
                .urls(urlsParam)
                .tempKey(tempKeyParam)
                .build();
    }
}
