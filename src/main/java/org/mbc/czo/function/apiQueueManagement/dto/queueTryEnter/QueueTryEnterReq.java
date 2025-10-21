package org.mbc.czo.function.apiQueueManagement.dto.queueTryEnter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueTryEnterReq {

    @NotNull
    private Long itemId;

    @NotNull
    private String userId;
}
