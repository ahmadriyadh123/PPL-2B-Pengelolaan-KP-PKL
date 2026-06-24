package com.jtk.ps.api.java.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtk.ps.api.controller.SeminarController;
import com.jtk.ps.api.dto.SeminarCriteriaRequestDto;
import com.jtk.ps.api.exception.GlobalExceptionHandler;
import com.jtk.ps.api.service.SeminarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UT015_016_SeminarControllerValidationTest {

    private MockMvc mockMvc;

    @Mock
    private SeminarService seminarService;

    @InjectMocks
    private SeminarController seminarController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(seminarController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateSeminarCriteria_InvalidPayload() throws Exception {
        // BUG-019 UT-015
        SeminarCriteriaRequestDto requestDto = new SeminarCriteriaRequestDto();
        requestDto.setCriteriaName(""); // Invalid blank name
        requestDto.setCriteriaBobot(-5.0f); // Just a value
        requestDto.setIsSelected(1);

        mockMvc.perform(post("/seminar/criteria")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.criteriaName").exists());
    }

    @Test
    void testCreateSeminarCriteria_ValidPayload() throws Exception {
        // BUG-019 UT-016
        SeminarCriteriaRequestDto requestDto = new SeminarCriteriaRequestDto();
        requestDto.setCriteriaName("Presentasi");
        requestDto.setCriteriaBobot(0.3f);
        requestDto.setIsSelected(1);

        mockMvc.perform(post("/seminar/criteria")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }
}
