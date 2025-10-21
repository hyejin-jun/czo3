package org.mbc.czo.function.product.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PageAdminRequestDto {

    private int page = 1;

    private int size = 10;
}
