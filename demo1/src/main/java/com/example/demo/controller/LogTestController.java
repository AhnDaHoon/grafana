package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/log-test")
public class LogTestController {

    private final Random random = new Random();

    /**
     * 모든 로그 레벨 테스트
     */
    @GetMapping("/all-levels")
    public ResponseEntity<Map<String, String>> testAllLogLevels() {
        String traceId = UUID.randomUUID().toString();

        log.trace("TRACE 레벨 로그 - TraceId: {}", traceId);
        log.debug("DEBUG 레벨 로그 - TraceId: {}", traceId);
        log.info("INFO 레벨 로그 - TraceId: {}", traceId);
        log.warn("WARN 레벨 로그 - TraceId: {}", traceId);
        log.error("ERROR 레벨 로그 - TraceId: {}", traceId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "모든 로그 레벨이 기록되었습니다");
        response.put("traceId", traceId);

        return ResponseEntity.ok(response);
    }

    /**
     * INFO 레벨 로그 생성
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> testInfo(@RequestParam(defaultValue = "1") int count) {
        for (int i = 1; i <= count; i++) {
            log.info("INFO 로그 #{} - 요청 시간: {}, 사용자: user_{}",
                i, LocalDateTime.now(), random.nextInt(1000));
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", count + "개의 INFO 로그가 생성되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * ERROR 레벨 로그 및 예외 스택 트레이스
     */
    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> testError() {
        try {
            // 의도적으로 예외 발생
            throw new RuntimeException("테스트 예외입니다 - 시간: " + LocalDateTime.now());
        } catch (Exception e) {
            log.error("예외가 발생했습니다", e);
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "ERROR 로그와 스택 트레이스가 기록되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * 구조화된 로그 (JSON 형태 데이터)
     */
    @PostMapping("/structured")
    public ResponseEntity<Map<String, Object>> testStructuredLog(@RequestBody(required = false) Map<String, Object> requestData) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        String userId = "USER-" + random.nextInt(10000);
        double amount = Math.round(random.nextDouble() * 100000 * 100.0) / 100.0;

        log.info("주문 생성 - OrderId: {}, UserId: {}, Amount: {}, Status: {}",
            orderId, userId, amount, "PENDING");

        log.info("결제 시작 - OrderId: {}, PaymentMethod: {}", orderId, "CARD");

        // 50% 확률로 성공/실패
        boolean success = random.nextBoolean();
        if (success) {
            log.info("결제 성공 - OrderId: {}, Amount: {}, TransactionId: {}",
                orderId, amount, "TXN-" + UUID.randomUUID().toString().substring(0, 8));
        } else {
            log.warn("결제 실패 - OrderId: {}, Reason: {}", orderId, "카드 승인 거부");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", success ? "success" : "failed");
        response.put("orderId", orderId);
        response.put("amount", amount);
        response.put("message", "구조화된 로그가 생성되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * 대량 로그 생성
     */
    @GetMapping("/bulk")
    public ResponseEntity<Map<String, Object>> testBulkLogs(
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "INFO") String level) {

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= count; i++) {
            String message = String.format("대량 로그 테스트 #%d - 요청시각: %s, 랜덤값: %d",
                i, LocalDateTime.now(), random.nextInt(10000));

            switch (level.toUpperCase()) {
                case "TRACE" -> log.trace(message);
                case "DEBUG" -> log.debug(message);
                case "INFO" -> log.info(message);
                case "WARN" -> log.warn(message);
                case "ERROR" -> log.error(message);
                default -> log.info(message);
            }
        }

        long endTime = System.currentTimeMillis();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", count);
        response.put("level", level);
        response.put("duration", (endTime - startTime) + "ms");
        response.put("message", count + "개의 " + level + " 로그가 생성되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * 시뮬레이션: 실제 서비스 로그 패턴
     */
    @PostMapping("/simulate-service")
    public ResponseEntity<Map<String, Object>> simulateServiceLog() {
        String requestId = UUID.randomUUID().toString();
        String userId = "USER-" + random.nextInt(10000);
        String endpoint = "/api/orders/create";

        log.info("[REQUEST] {} - User: {}, Endpoint: {}", requestId, userId, endpoint);

        // 비즈니스 로직 시뮬레이션
        log.debug("[PROCESS] {} - 사용자 정보 조회 중...", requestId);
        log.debug("[PROCESS] {} - 재고 확인 중...", requestId);
        log.debug("[PROCESS] {} - 가격 계산 중...", requestId);

        // 랜덤 처리 시간
        int processingTime = 50 + random.nextInt(200);

        // 80% 확률로 성공
        if (random.nextInt(100) < 80) {
            log.info("[SUCCESS] {} - 처리 완료, 소요시간: {}ms", requestId, processingTime);
        } else {
            log.error("[FAILED] {} - 처리 실패, ErrorCode: {}, 소요시간: {}ms",
                requestId, "ERR-" + random.nextInt(100), processingTime);
        }

        log.info("[RESPONSE] {} - 응답 전송 완료", requestId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("requestId", requestId);
        response.put("message", "서비스 로그 시뮬레이션이 완료되었습니다");

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 키워드가 포함된 로그 생성 (검색 테스트용)
     */
    @GetMapping("/keyword")
    public ResponseEntity<Map<String, String>> testKeywordLog(@RequestParam String keyword) {
        log.info("키워드 검색 테스트 - 검색어: [{}], 결과수: {}", keyword, random.nextInt(100));
        log.warn("키워드 필터 적용 - 필터: [{}], 제외된 항목: {}", keyword, random.nextInt(10));
        log.debug("키워드 인덱싱 - 인덱스: [{}], 처리시간: {}ms", keyword, random.nextInt(50));

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("keyword", keyword);
        response.put("message", "키워드가 포함된 로그가 생성되었습니다");

        return ResponseEntity.ok(response);
    }
}
