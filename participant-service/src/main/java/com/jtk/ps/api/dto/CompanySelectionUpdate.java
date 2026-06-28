package com.jtk.ps.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySelectionUpdate {
    
    @NotBlank(message = "Work system cannot be empty")
    @Pattern(regexp = "^(Full Time|Part Time|Internship|Contract|Temporary)$", 
             message = "Work system must be one of: Full Time, Part Time, Internship, Contract, Temporary")
    @JsonProperty("work_system")
    private String workSystem;

    @NotNull(message = "Priority 1 cannot be empty")
    @Min(value = 1, message = "Priority 1 must be at least 1")
    @Max(value = 999999, message = "Priority 1 must be a valid company ID")
    private Integer priority1;

    @Min(value = 1, message = "Priority 2 must be at least 1 or null")
    @Max(value = 999999, message = "Priority 2 must be a valid company ID")
    private Integer priority2;

    @Min(value = 1, message = "Priority 3 must be at least 1 or null")
    @Max(value = 999999, message = "Priority 3 must be a valid company ID")
    private Integer priority3;

    @Min(value = 1, message = "Priority 4 must be at least 1 or null")
    @Max(value = 999999, message = "Priority 4 must be a valid company ID")
    private Integer priority4;

    @Min(value = 1, message = "Priority 5 must be at least 1 or null")
    @Max(value = 999999, message = "Priority 5 must be a valid company ID")
    private Integer priority5;
}
