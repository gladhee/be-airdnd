package rice_monkey.booking.common.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisBusyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rice_monkey.booking.common.advice.dto.ProblemDetail;
import rice_monkey.booking.exception.business.BusinessException;
import rice_monkey.booking.exception.infra.InfrastructureException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 비즈니스 예외 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("BUSINESS ERROR [{}] URI={} user={} ", ex.getErrorCode(),
                req.getRequestURI(), req.getHeader("X-User-Id"), ex);

        return problemResponse(
                ex.getHttpStatus(),
                "https://api.booking.com/errors/" + ex.getErrorCode(),
                ex.getMessage(),
                req.getRequestURI(),
                ex.getErrorCode()
        );
    }

    /** 인프라 예외 */
    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ProblemDetail> handleInfrastructure(InfrastructureException ex, HttpServletRequest req) {
        log.error("INFRA ERROR [{}] URI={} user={}", ex.getErrorCode(),
                req.getRequestURI(), req.getHeader("X-User-Id"), ex);

        return problemResponse(
                ex.getHttpStatus(),
                "https://api.booking.com/errors/" + ex.getErrorCode(),
                "내부 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                req.getRequestURI(),
                ex.getErrorCode()
        );
    }

    /** Redis Busy */
    @ExceptionHandler(RedisBusyException.class)
    public ResponseEntity<ProblemDetail> handleRedisBusy(RedisBusyException ex, HttpServletRequest req) {
        log.warn("LOCK BUSY [{}] URI={} user={}", "BOOKING_LOCK_BUSY",
                req.getRequestURI(), req.getHeader("X-User-Id"), ex);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .header("Retry-After", "2")
                .body(new ProblemDetail(
                        "https://api.booking.com/errors/BOOKING_LOCK_BUSY",
                        "현재 다른 예약 처리 중입니다. 잠시 후 다시 시도해 주세요.",
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        ex.getMessage(),
                        req.getRequestURI(),
                        "BOOKING_LOCK_BUSY"
                ));
    }

    /** Validation 오류 (DTO @Valid 실패) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst().orElse("Validation failed");

        log.debug("VALIDATION ERROR URI={} detail={}", req.getRequestURI(), detail);

        return problemResponse(
                HttpStatus.BAD_REQUEST,
                "https://api.booking.com/errors/INVALID_REQUEST",
                "요청 값이 올바르지 않습니다.",
                req.getRequestURI(),
                "INVALID_REQUEST"
        );
    }

    /** Binding 오류 (형변환 실패, 예: 헤더에 문자열 넣었는데 Long 매핑 실패) */
    @ExceptionHandler({BindException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ProblemDetail> handleBinding(Exception ex, HttpServletRequest req) {
        log.debug("BINDING ERROR URI={} msg={}", req.getRequestURI(), ex.getMessage());

        return problemResponse(
                HttpStatus.BAD_REQUEST,
                "https://api.booking.com/errors/BAD_REQUEST",
                "잘못된 요청입니다.",
                req.getRequestURI(),
                "BAD_REQUEST"
        );
    }

    /** 그 외 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("GENERAL ERROR URI={} user={}", req.getRequestURI(), req.getHeader("X-User-Id"), ex);

        return problemResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "https://api.booking.com/errors/GENERAL_ERROR",
                "예기치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                req.getRequestURI(),
                "GENERAL_ERROR"
        );
    }

    private ResponseEntity<ProblemDetail> problemResponse(
            HttpStatus status, String type, String title,
            String instance, String code
    ) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(new ProblemDetail(type, title, status.value(), title, instance, code));
    }

}
