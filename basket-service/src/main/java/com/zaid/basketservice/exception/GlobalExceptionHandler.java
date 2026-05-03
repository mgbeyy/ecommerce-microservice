package com.zaid.basketservice.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Ürün bulunamadığında (Product Service 404 döndüğünde) Feign'in fırlattığı hata
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleFeignNotFoundException(
            FeignException.NotFound ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "İşlem yapmak istediğiniz ürün sistemde bulunamadı.", // Statik ve güvenli mesaj
                request.getRequestURI(),
                getTraceId(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // 2. Beklenmeyen diğer tüm sistem hataları (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Sunucu tarafında beklenmeyen bir hata oluştu.",
                request.getRequestURI(),
                getTraceId(request)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Ortak Trace-Id çıkarıcı yardımcı metot
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("Trace-Id");
        return (traceId != null && !traceId.isBlank()) ? traceId : "trace-id-bulunamadi";
    }
}