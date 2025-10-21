package org.mbc.czo.function.apiUploadImage.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRef {

    List<MultipartFile> files;
    Map<String, String> extraData;

}
