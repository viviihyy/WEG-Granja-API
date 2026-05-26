package com.weg.granja.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroResponse> handleNegocio(NegocioException ex) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(new ErroResponse(ex.getStatus().value(), ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            erros.put(campo, error.getDefaultMessage());
        });
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErroResponse(400, "Erro de validação: " + erros, LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGeneric(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErroResponse(500, "Erro interno: " + ex.getMessage(), LocalDateTime.now()));
    }

    public record ErroResponse(int status, String mensagem, LocalDateTime timestamp) {}
}
