package com.weg.granja.exception;

import org.springframework.http.HttpStatus;

public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    public NegocioException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status;
    }

    public NegocioException(String mensagem) {
        this(mensagem, HttpStatus.BAD_REQUEST);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
