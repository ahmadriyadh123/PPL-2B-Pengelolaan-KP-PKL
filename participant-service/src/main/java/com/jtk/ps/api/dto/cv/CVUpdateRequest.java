package com.jtk.ps.api.dto.cv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jtk.ps.api.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
public class CVUpdateRequest {
    
    @NotBlank(message = "Nickname cannot be empty")
    @Size(min = 2, max = 100, message = "Nickname must be between 2 and 100 characters")
    private String nickname;

    @NotBlank(message = "Address cannot be empty")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$|^0\\d{9,14}$", message = "Phone number format is invalid")
    @JsonProperty("no_phone")
    private String noPhone;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Religion cannot be empty")
    @Size(min = 2, max = 50, message = "Religion must be between 2 and 50 characters")
    private String religion;

    @NotNull(message = "Gender cannot be empty")
    private Character gender;

    @NotBlank(message = "Birth place cannot be empty")
    @Size(min = 2, max = 100, message = "Birth place must be between 2 and 100 characters")
    private String place;

    @NotBlank(message = "Birthday cannot be empty")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Birthday format must be YYYY-MM-DD")
    private String birthday;

    @NotNull(message = "Marriage status cannot be empty")
    private Boolean marriage;

    @NotBlank(message = "Citizenship cannot be empty")
    @Size(min = 2, max = 50, message = "Citizenship must be between 2 and 50 characters")
    private String citizenship;

    @NotNull(message = "Domicile ID cannot be empty")
    @Positive(message = "Domicile ID must be a positive number")
    @JsonProperty("domicile_id")
    private Integer domicileId;

    @Valid
    @Size(min = 1, message = "At least one education record is required")
    private List<Education> educations;

    @Valid
    private List<Experience> experiences;

    @Valid
    private List<Organization> organizations;

    @Valid
    private List<CVCompetence> competencies;

    @Valid
    private List<CVJobScope> jobscopes;

    @Valid
    private List<Skill> skills;

    @Valid
    private List<Seminar> seminars;

    @Valid
    private List<Championship> championships;
}
