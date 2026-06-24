package com.jtk.ps.api.java.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtk.ps.api.dto.SeminarCriteriaRequestDto;
import com.jtk.ps.api.model.EventStore;
import com.jtk.ps.api.model.SeminarCriteria;
import com.jtk.ps.api.repository.EventStoreRepository;
import com.jtk.ps.api.repository.SeminarCriteriaRepository;
import com.jtk.ps.api.service.SeminarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UT012_SeminarServiceJsonProcessingExceptionTest {

    @Mock
    private EventStoreRepository eventStoreRepository;

    @Mock
    private SeminarCriteriaRepository seminarCriteriaRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SeminarService seminarService;

    @Test
    void testEventStoreHandler_WhenJsonProcessingException_ShouldRethrowRuntimeException() throws Exception {
        // BUG-012: Memastikan cabang JsonProcessingException pada eventStoreHandler() tertangani secara terpisah
        SeminarCriteriaRequestDto request = new SeminarCriteriaRequestDto();
        request.setCriteriaName("Test");
        request.setCriteriaBobot(0.5f);
        request.setIsSelected(1);

        SeminarCriteria savedCriteria = new SeminarCriteria();
        savedCriteria.setId(1);
        
        when(seminarCriteriaRepository.save(any(SeminarCriteria.class))).thenReturn(savedCriteria);
        
        // Mock objectMapper to throw JsonProcessingException
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {});

        assertThrows(RuntimeException.class, () -> seminarService.createSeminarCriteria(request));

        verify(eventStoreRepository, never()).save(any(EventStore.class));
    }
}
