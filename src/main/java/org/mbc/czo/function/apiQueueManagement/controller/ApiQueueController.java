package org.mbc.czo.function.apiQueueManagement.controller;

import lombok.RequiredArgsConstructor;

import org.mbc.czo.function.apiQueueManagement.dto.queueGetPos.QueueGetPosReq;
import org.mbc.czo.function.apiQueueManagement.dto.queueGetPos.QueueGetPosRes;
import org.mbc.czo.function.apiQueueManagement.dto.queueTryEnter.QueueTryEnterReq;
import org.mbc.czo.function.apiQueueManagement.dto.queueTryEnter.QueueTryEnterRes;
import org.mbc.czo.function.apiQueueManagement.service.ApiQueueService;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queue")
public class ApiQueueController {

    private final ApiQueueService queueService;

    //대기열 참가
    @PostMapping("/try-enter")
    public ApiResult<QueueTryEnterRes> enterQueue(@RequestBody QueueTryEnterReq queueTryEnterReq) {

        try {
            Long itemId = queueTryEnterReq.getItemId();
            String userId = queueTryEnterReq.getUserId();
            queueService.joinQueue(itemId, userId); // 대기열 등록
            QueueTryEnterRes queueTryEnterRes =
                    QueueTryEnterRes.createQueueTryEnterRes(queueService.getQueuePosition(userId));

            return ApiResult.created(queueTryEnterRes);

        } catch (Exception e) {

            return ApiResult.fail(e.getMessage());
        }
    }



    //현재 대기 순번 조회
    @PostMapping("/position")
    public ApiResult<QueueGetPosRes> getPosition(@RequestBody QueueGetPosReq queueGetPosReq) {

        try {
            QueueGetPosRes queueGetPosRes =
                    QueueGetPosRes.createQueueGetPosRes(queueService.getQueuePosition(queueGetPosReq.getUserId()));

            return ApiResult.created(queueGetPosRes);

        } catch (Exception e) {

            return ApiResult.fail(e.getMessage());
        }
    }

    //SSE 구독 (입장 신호 기다림)
    @GetMapping("/sse")
    public SseEmitter subscribe(@RequestParam String userId) {

        try {

            return queueService.sseSubscribe(userId);

        } catch (Exception e) {
            // 연결 생성 실패 시 null 반환 또는 빈 SseEmitter 반환
            System.err.println("SSE 구독 실패: " + e.getMessage());
            return new SseEmitter(0L); // 최소한 연결 끊김 없이 빈 emitter 반환
        }
    }
}
