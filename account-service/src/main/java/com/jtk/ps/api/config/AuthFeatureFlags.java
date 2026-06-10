package com.jtk.ps.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Feature flags untuk modul autentikasi.
 *
 * <p>S2-T04 (PB-12) — Persiapan Sprint 3: migrasi 5 service downstream ke validasi JWT lokal.
 * Flag ini memungkinkan rollback tanpa redeploy — cukup set env variable
 * {@code AUTH_LOCAL_VALIDATION_ENABLED=false} lalu restart service.
 *
 * <p>Cara penggunaan:
 * <pre>
 *     {@literal @}Autowired
 *     private AuthFeatureFlags authFeatureFlags;
 *
 *     if (authFeatureFlags.isLocalValidationEnabled()) {
 *         // validasi JWT secara lokal
 *     } else {
 *         // delegasi ke account-service (behaviour lama)
 *     }
 * </pre>
 */
@Component
public class AuthFeatureFlags {

    /**
     * Mengaktifkan validasi JWT lokal di service downstream.
     *
     * <p>Default {@code false} — behaviour lama (delegasi ke account-service) tetap dipakai
     * sampai Sprint 3 selesai dan semua service sudah diverifikasi.
     * Override via env variable: {@code AUTH_LOCAL_VALIDATION_ENABLED=true}
     */
    @Value("${auth.local-validation.enabled:false}")
    private boolean localValidationEnabled;

    public boolean isLocalValidationEnabled() {
        return localValidationEnabled;
    }
}