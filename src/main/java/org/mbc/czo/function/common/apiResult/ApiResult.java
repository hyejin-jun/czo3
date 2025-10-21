package org.mbc.czo.function.common.apiResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResult<T> {

    private boolean success;    // 성공 여부
    private String message;     // 상태 메시지
    private int code;           // HTTP 상태 코드나 커스텀 코드
    private T data;             // 실제 응답 데이터

    // ==================== 헬퍼 메서드 ====================

    // 일반 조회/성공 (GET, PUT, PATCH)
    //200 OK
    public static <T> ApiResult<T> ok(T data) {
        return ApiResult.<T>builder()
                .success(true)
                .message("success")
                .code(HttpStatus.OK.value())
                .data(data)
                .build();
    }

    // POST 생성 성공 (POST)
    //201 Created
    public static <T> ApiResult<T> created(T data) {
        return ApiResult.<T>builder()
                .success(true)
                .message("created")
                .code(HttpStatus.CREATED.value())
                .data(data)
                .build();
    }

    // 성공하지만 반환 데이터 없음 (DELETE, PUT/PATCH (데이터 반환 없이 성공))
    //204 No Content
    public static <T> ApiResult<T> none() {
        return ApiResult.<T>builder()
                .success(true)
                .message("no content")
                .code(HttpStatus.NO_CONTENT.value())
                .data(null)
                .build();
    }

    // 실패 응답 (요청 실패, 클라이언트 오류 등)
    //400 Bad Request
    public static <T> ApiResult<T> fail(String message) {
        return ApiResult.<T>builder()
                .success(false)
                .message(message)
                .code(HttpStatus.BAD_REQUEST.value())
                .data(null)
                .build();
    }

/*  정리
    HTTP 메서드   Spring 어노테이션   용도            멱등성
    POST        @PostMapping     새 리소스 생성       ❌
    PUT         @PutMapping      리소스 전체 수정     ✅
    PATCH       @PatchMapping    리소스 일부 수정   부분적으로 ✅*/

   /* 팁:

    생성 → POST

    전체 수정 → PUT

    부분 수정 → PATCH

    삭제 → DELETE*/

    /*
        보안 문제로 Get, Post 제외하고 안쓴다는 현업자 조언도 있음
        but Restful한 구조를 위해서는 HTTP 메서드 활용 필요
    */

    //성공시 json 데이터 응답 예시
   /* {
        "success": true,
            "message": "created",
            "code": 201,
            "data<T>": {
                //T에 대한 dto값
                "id": 5,
                "username": "alice",
                "email": "alice@example.com"
                }
    }*/

    //성공시 json 데이터 응답 예시
    /*{
        "success": false,
            "message": "Username already exists",
            "code": 400,
            "data": null
    }*/

}
