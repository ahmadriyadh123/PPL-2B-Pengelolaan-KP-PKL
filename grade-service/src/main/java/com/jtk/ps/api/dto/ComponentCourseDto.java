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
public class ComponentCourseDto {
    
    private Integer id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Bobot component is required")
    @JsonProperty("bobot_component")
    private Integer bobotComponent;

    @NotNull(message = "Course ID is required")
    @JsonProperty("course_id")
    private Integer courseId;

    @JsonProperty("is_average")
    private Integer isAverage;
}
