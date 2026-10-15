package edu.dosw.parcial.controller.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import edu.dosw.parcial.core.exceptions.ConflictException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_retorna400() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("request", "name", "obligatorio")));

        // Act
        HttpStatus status = (HttpStatus) handler.handleValidation(exception).getStatusCode();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, status);
    }

    @Test
    void handleUnreadable_retorna400() {
        // Arrange
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        // Act
        HttpStatus status = (HttpStatus) handler.handleUnreadable(exception).getStatusCode();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, status);
    }

    @Test
    void handleConflict_retorna409() {
        // Arrange
        ConflictException exception = new ConflictException("duplicado");

        // Act
        HttpStatus status = (HttpStatus) handler.handleConflict(exception).getStatusCode();

        // Assert
        assertEquals(HttpStatus.CONFLICT, status);
    }

    @Test
    void handleUnexpected_retorna500() {
        // Arrange
        Exception exception = new RuntimeException("falla");

        // Act
        HttpStatus status = (HttpStatus) handler.handleUnexpected(exception).getStatusCode();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, status);
    }
}