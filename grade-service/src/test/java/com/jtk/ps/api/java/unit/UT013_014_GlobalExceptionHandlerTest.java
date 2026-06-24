package com.jtk.ps.api.java.unit;

import com.jtk.ps.api.dto.ErrorResponse;
import com.jtk.ps.api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UT013_014_GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleValidationException() {
        // BUG-018 & BUG-019 UT-013
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/seminar/criteria/create");

        MethodParameter parameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("object", "participantId", "participantId wajib diisi"),
                new FieldError("object", "criteriaName", "criteriaName tidak boleh kosong"),
                new FieldError("object", "maxScore", "maxScore minimal 0")
        );
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldErrors.toArray(new FieldError[0])));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse errorResponse = response.getBody();

        // Verifikasi message
        assertNotNull(errorResponse.getValidationErrors());
        assertTrue(errorResponse.getValidationErrors().containsKey("participantId"));
        assertEquals("participantId wajib diisi", errorResponse.getValidationErrors().get("participantId"));
        
        // Verifikasi format status
        assertEquals(400, errorResponse.getStatus());
        
        String responseStr = errorResponse.toString();
        // Verifikasi tidak ada nama tabel atau stack trace DB
        assertFalse(responseStr.contains("Table"));
        assertFalse(responseStr.contains("Constraint"));
    }

    @Test
    void testHandleGenericException() {
        // BUG-018 UT-014
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/seminar/criteria/create");

        Exception exception = new Exception("Table seminar_criteria not found");

        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleGenericException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        ErrorResponse errorResponse = responseEntity.getBody();

        // Memastikan field message mengandung pesan generik yang aman
        assertTrue(errorResponse.getMessage() != null && 
                  (errorResponse.getMessage().toLowerCase().contains("an unexpected error occurred") || 
                   errorResponse.getMessage().toLowerCase().contains("internal server error")));

        String responseStr = errorResponse.toString();
        // Verifikasi tidak ada nama tabel dalam JSON yg direturn
        assertFalse(responseStr.contains("Table seminar_criteria not found"));
    }
}
