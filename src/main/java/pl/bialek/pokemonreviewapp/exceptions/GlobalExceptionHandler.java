package pl.bialek.pokemonreviewapp.exceptions;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Map<Class<?>, HttpStatus> EXCEPTION_STATUS = Map.of(
            ConstraintViolationException.class, HttpStatus.BAD_REQUEST,
            EntityNotFoundException.class, HttpStatus.NOT_FOUND,
            ReviewNotFoundException.class, HttpStatus.NOT_FOUND,
            PokemonNotFoundException.class, HttpStatus.NOT_FOUND
            );

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex,
            Object body,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {

        final String errorId = UUID.randomUUID().toString();
        log.error("Exceptin: ID={}, HttpStatus={}", errorId, statusCode, ex);
        return super.handleExceptionInternal(
                ex,
                ExceptionMessage.of(errorId, ex.getMessage(), new Date()),
                headers,
                statusCode,
                request
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception exception){
        return doHandle(exception, getHttpStatusFromException(exception.getClass()));
    }

    private ResponseEntity<?> doHandle(Exception exception, HttpStatus statusCode){
        final String errorId = UUID.randomUUID().toString();
        log.error("Exceptin: ID={}, HttpStatus={}", errorId, statusCode, exception);
        return ResponseEntity
                .status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ExceptionMessage.of(errorId,exception.getMessage(),new Date()));
    }
    private HttpStatus getHttpStatusFromException(final Class<?> exceptionClass){
        return EXCEPTION_STATUS.getOrDefault(
                exceptionClass,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    @ExceptionHandler({PokemonNotFoundException.class, ReviewNotFoundException.class})
    public ResponseEntity<?> handleNotFound(Exception exception, WebRequest request){
        HttpStatus status = HttpStatus.NOT_FOUND;
        final String errorId = UUID.randomUUID().toString();
        log.error("Exception: ID={}, HttpStatus={}, ExceptionType={}", errorId, status, exception.getClass().getSimpleName(), exception);
        ExceptionMessage exceptionMessage = ExceptionMessage.of(errorId,exception.getMessage(),new Date());
        exceptionMessage.setMessage(exception.getMessage());
        exceptionMessage.setTimestamp(new Date());

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(exceptionMessage);
    }



}