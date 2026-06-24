package com.jtk.ps.api.java.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtk.ps.api.controller.SeminarController;
import com.jtk.ps.api.dto.SeminarCriteriaRequestDto;
import com.jtk.ps.api.service.SeminarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jtk.ps.api.security.WebSecurityConfigurerAdapter;
import com.jtk.ps.api.security.AuthenticationFilter;
import org.springframework.context.annotation.Import;
import com.jtk.ps.api.service.Interface.ISeminarService;

@WebMvcTest(value = SeminarController.class, properties = "spring.main.allow-bean-definition-overriding=true")
@Import(WebSecurityConfigurerAdapter.class)
public class UT009_SecurityPreAuthorizeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISeminarService seminarService;

    @org.springframework.boot.test.context.TestConfiguration
    static class DummySecurityConfig {
        @org.springframework.context.annotation.Bean
        public AuthenticationFilter authenticationFilter() {
            return new AuthenticationFilter() {
                @Override
                protected void doFilterInternal(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, javax.servlet.FilterChain filterChain) throws javax.servlet.ServletException, java.io.IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(authorities = "PARTICIPANT")
    void testCreateSeminarCriteria_WithParticipantRole_ShouldReturnForbidden() throws Exception {
        // BUG-009 
        SeminarCriteriaRequestDto requestDto = new SeminarCriteriaRequestDto();
        requestDto.setCriteriaName("Presentasi");
        requestDto.setCriteriaBobot(0.3f);
        requestDto.setIsSelected(1);

        mockMvc.perform(post("/seminar/criteria")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "COMMITTEE")
    void testCreateSeminarCriteria_WithCommitteeRole_ShouldReturnOk() throws Exception {
        // BUG-009 
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
