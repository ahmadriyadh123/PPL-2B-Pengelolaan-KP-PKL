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
public class CourseFormRequestDto {
    
    @NotNull(message = "Prodi ID is required")
    @JsonProperty("prodi_id")
    private Integer prodiId;

    @NotBlank(message = "Kode is required")
    @JsonProperty("kode")
    private String kode;

    @NotBlank(message = "Name is required")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "Tahun ajaran start is required")
    @JsonProperty("tahun_ajaran_start")
    private Integer tahunAjaranStart;

    @NotNull(message = "Tahun ajaran end is required")
    @JsonProperty("tahun_ajaran_end")
    private Integer tahunAjaranEnd;

    @NotNull(message = "SKS is required")
    @JsonProperty("sks")
    private Integer sks;
}
