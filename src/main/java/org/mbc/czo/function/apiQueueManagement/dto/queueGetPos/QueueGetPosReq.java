package org.mbc.czo.function.apiQueueManagement.dto.queueGetPos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueGetPosReq {

    @NotNull
    private String userId;
}
