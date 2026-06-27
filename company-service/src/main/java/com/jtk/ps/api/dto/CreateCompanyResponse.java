package com.jtk.ps.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response saat membuat company baru.
 * Memuat username dan password awal akun company agar panitia dapat
 * menyampaikannya ke pihak company. Password ini hanya dikembalikan
 * sekali, yaitu saat akun pertama kali dibuat.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyResponse {
    @JsonProperty("company_id")
    private Integer companyId;

    @JsonProperty("company_email")
    private String companyEmail;

    private String username;

    private String password;
}
