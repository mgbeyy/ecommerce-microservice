package com.zaid.gatewayserver.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // JWT ile ilgili herhangi bir hata (Süresi dolmuş, imza bozuk, token geçersiz) gelirse buraya düşecek
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Yetkisiz erişim: Token geçersiz veya süresi dolmuş.",
                request
        );
    }

    // 1. Yanlış URL veya Gateway'de tanımlı olmayan bir route'a istek gelirse (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "İstek atılan route Gateway üzerinde bulunamadı.", request);
    }

    // 2. İstek alt servise yönlendirildi ama alt servis kapalıysa (503)
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleDownstreamDownException(ResourceAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Hedef mikroservis şu anda yanıt vermiyor.", request);
    }

    // 3. Beklenmeyen diğer tüm hatalar (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Gateway seviyesinde beklenmeyen bir hata oluştu: " + ex.getMessage(), request);
    }



    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        // Trace ID'yi Gateway'de hata anında oluşturuyoruz.
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}