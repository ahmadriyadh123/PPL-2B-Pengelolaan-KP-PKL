package com.jtk.ps.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RekapDokumenResponse {
    private Integer id;
    private String nim;
    private String name;
    private String company;
    private String status;
}
