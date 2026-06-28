package com.jtk.ps.api.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValuationV2Dto {
    private Integer id;

    private String aspectName;

    @NotNull(message = "Numeric value is required")
    private Integer numericValue;

    private String letterValue;

    private String justification;

    @NotNull(message = "Evaluation ID is required")
    private Integer evaluationId;
}
