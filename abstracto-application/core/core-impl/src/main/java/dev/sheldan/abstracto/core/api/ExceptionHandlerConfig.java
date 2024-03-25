package dev.sheldan.abstracto.core.api;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerConfig {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(GuildNotFoundException.class)
    protected ResponseEntity<String> handleResourceNotFound(GuildNotFoundException ex){
        log.warn("Server not found.", ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Server not found");
    }
}
