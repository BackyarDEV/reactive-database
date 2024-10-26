package com.backyardev.reactivedatabase.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Component
@Order(-100)
@Slf4j
@RequiredArgsConstructor
public class RestExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        log.debug("Handling exceptions: {}", ex.getClass().getSimpleName(), ex);
        var errors = new HashMap<>();
        errors.put("status", "ERROR");
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        if (ex instanceof ReactiveAppException e) {
            if (e.getStatus() != null)
                exchange.getResponse().setStatusCode(e.getStatus());
            errors.put("errorMessage", e.getErrorMessage());
        } else {
            errors.put("errorMessage", ex.getMessage());
        }
        var db = new DefaultDataBufferFactory().wrap(objectMapper.writeValueAsBytes(errors));
        return exchange.getResponse().writeWith(Mono.just(db));
    }

}
