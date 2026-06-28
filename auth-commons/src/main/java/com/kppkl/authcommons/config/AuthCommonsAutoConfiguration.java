package com.kppkl.authcommons.config;

import com.kppkl.authcommons.filter.StatelessJwtAuthenticationFilter;
import com.kppkl.authcommons.reader.CookieTokenReader;
import com.kppkl.authcommons.validator.JwtTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * S3-T01 — Auto-konfigurasi untuk auth-commons.
 *
 * Spring Boot akan memuat kelas ini secara otomatis melalui
 * META-INF/spring.factories tanpa perlu @ComponentScan tambahan
 * di service yang menggunakan library ini.
 *
 * @ConditionalOnMissingBean memastikan service yang sudah punya
 * implementasi sendiri tidak tertimpa oleh kelas ini.
 *
 * Property yang wajib ada di application.properties service pengguna:
 *   jwt.secret=<min 64 karakter>
 *
 * Property opsional:
 *   jwt.accessTokenCookieName=accessToken  (default: accessToken)
 *   auth.public-paths=/v2/api-docs,/actuator/health
 */
@Slf4j
@Configuration
public class AuthCommonsAutoConfiguration {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.accessTokenCookieName:accessToken}")
    private String accessTokenCookieName;

    /**
     * Daftar path publik yang tidak memerlukan JWT.
     * Dibaca dari property auth.public-paths, dipisahkan koma.
     * Mendukung Ant pattern (mis. /form-submit-time/**)
     */
    @Value("${auth.public-paths:}")
    private String publicPathsRaw;

    @Bean
    @ConditionalOnMissingBean(JwtTokenValidator.class)
    public JwtTokenValidator jwtTokenValidator() {
        log.info("[AuthCommons] Membuat JwtTokenValidator bean");
        return new JwtTokenValidator(jwtSecret);
    }

    @Bean
    @ConditionalOnMissingBean(CookieTokenReader.class)
    public CookieTokenReader cookieTokenReader() {
        log.info("[AuthCommons] Membuat CookieTokenReader bean, cookieName='{}'",
                accessTokenCookieName);
        return new CookieTokenReader(accessTokenCookieName);
    }

    @Bean
    @ConditionalOnMissingBean(StatelessJwtAuthenticationFilter.class)
    public StatelessJwtAuthenticationFilter statelessJwtAuthenticationFilter(
            JwtTokenValidator jwtTokenValidator,
            CookieTokenReader cookieTokenReader) {

        List<String> publicPaths = parsePublicPaths(publicPathsRaw);
        log.info("[AuthCommons] Membuat StatelessJwtAuthenticationFilter, publicPaths={}",
                publicPaths);
        return new StatelessJwtAuthenticationFilter(
                jwtTokenValidator, cookieTokenReader, publicPaths);
    }

    private List<String> parsePublicPaths(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(raw.split(","));
    }
}
