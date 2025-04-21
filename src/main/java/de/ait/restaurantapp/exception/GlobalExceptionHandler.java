package de.ait.restaurantapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoAvailableTableException.class)
    public ResponseEntity<String> handleNoTable(NoAvailableTableException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)   // 409 Conflict
                .body(ex.getMessage());
    }

}

