package br.com.escrevaevolua.identity.adapter.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escrevaevolua.identity.application.IdentityOperationException;

@RestControllerAdvice
public class IdentityOperationExceptionMapper {
    @ExceptionHandler(IdentityOperationException.class)
    public ResponseEntity<ErrorResponse> toResponse(IdentityOperationException exception) {
        HttpStatus status = exception.isInternalFailure() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ErrorResponse(exception.getMessage()));
    }

    public record ErrorResponse(String message) {}
}
