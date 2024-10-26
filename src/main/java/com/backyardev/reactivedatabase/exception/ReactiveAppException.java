package com.backyardev.reactivedatabase.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ReactiveAppException extends Exception {

    private HttpStatus status;
    private String errorMessage;

    public ReactiveAppException(Throwable ex) {
        super(ex);
        this.errorMessage = ex.getMessage();
    }

    public ReactiveAppException(String message) {
        this.errorMessage = message;
    }

    public ReactiveAppException(HttpStatus status, String errorMessage, Exception ex) {
        super(ex);
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public ReactiveAppException(HttpStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }
}
