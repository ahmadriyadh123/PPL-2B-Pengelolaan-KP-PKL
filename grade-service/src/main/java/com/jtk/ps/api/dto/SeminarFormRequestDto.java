package com.jtk.ps.api.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeminarFormRequestDto {

    @NotNull(message = "Participant ID is required")
    @JsonProperty("participant_id")
    private Integer participantId;
    
    @NotNull(message = "Date seminar is required")
    @JsonProperty("date_seminar")
    private Date dateSeminar;

    @NotNull(message = "Examiner ID is required")
    @JsonProperty("examiner_id")
    private Integer examinerId;

    @NotNull(message = "Examiner type is required")
    @JsonProperty("examiner_type")
    private Integer examinerType;

    @JsonProperty("comment")
    private String comment;
}
