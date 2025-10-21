package org.mbc.czo.function.apiUploadImage.dto.delete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDeleteRef {

    String storedFileName;
    Map<String, String> extraData;
}
