package com.jtk.ps.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeminarCriteriaRequestDto {
    
    @NotBlank(message = "Criteria name is required")
    @JsonProperty("criteria_name")
    private String criteriaName;

    @JsonProperty("criteria_bobot")
    private Float criteriaBobot;

    @JsonProperty("is_selected")
    private Integer isSelected;
}
