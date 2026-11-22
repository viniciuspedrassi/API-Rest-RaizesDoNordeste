package com.raizesDoNordeste.raizesDoNordeste.exceptions;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;


@RestControllerAdvice
public class GlobalExceptionHandler {

// TRATAMENTO - Retorna uma mensagem segura quando o corpo da requisição está ausente ou inválido.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(
            HttpMessageNotReadableException excecao,
            HttpServletRequest request) {

        Map<String, Object> erro = new LinkedHashMap<>();

        erro.put("timestamp", LocalDateTime.now());
        erro.put("status", 400);
        erro.put("error", "Bad Request");
        erro.put("message", "O corpo da requisição está ausente ou contém JSON inválido.");
        erro.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

}
