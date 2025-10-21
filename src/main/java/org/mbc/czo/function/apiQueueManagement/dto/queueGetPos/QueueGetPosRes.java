package org.mbc.czo.function.apiQueueManagement.dto.queueGetPos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiQueueManagement.dto.queueTryEnter.QueueTryEnterRes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueGetPosRes {

    private Long position;

    public static QueueGetPosRes createQueueGetPosRes(Long positionParam) {
        return QueueGetPosRes.builder()
                .position(positionParam)
                .build();
    }
}
