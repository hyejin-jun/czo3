package org.mbc.czo.function.apiQueueManagement.dto.queueTryEnter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiMember.dto.findId.MemberFindIdRes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueTryEnterRes {

    private Long position;

    public static QueueTryEnterRes createQueueTryEnterRes(Long positionParam) {
        return QueueTryEnterRes.builder()
                .position(positionParam)
                .build();
    }
}
