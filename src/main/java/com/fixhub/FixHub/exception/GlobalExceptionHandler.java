package com.fixhub.FixHub.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
        logger.warn("Erro de negócio: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        logger.warn("Estado inválido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        logger.warn("Erro com status {}: {}", ex.getStatus(), ex.getReason());
        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of("erro", ex.getReason() != null ? ex.getReason() : "Erro desconhecido"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Erro interno não tratado", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Ocorreu um erro interno. Contate o suporte."));
    }
}
