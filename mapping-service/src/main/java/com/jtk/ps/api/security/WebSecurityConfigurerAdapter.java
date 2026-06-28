package com.jtk.ps.api.security;

import com.kppkl.authcommons.filter.StatelessJwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * S3-T03 — Migrasi mapping-service ke auth-commons (Canary service pertama).
 *
 * Perubahan dari konfigurasi lama:
 * - AuthenticationFilter lama (138 LOC, HTTP call ke /account/verify) dihapus
 * - StatelessJwtAuthenticationFilter dari auth-commons digunakan sebagai pengganti
 * - Public paths (/v2/api-docs, /actuator/health) dikonfigurasi via application.properties
 *   (auth.public-paths) sehingga sesuai dengan shouldNotFilter() di StatelessJwtAuthenticationFilter
 * - SecurityContextLogoutHandler yang ada di filter lama ikut terhapus (sesuai S2-T08)
 * - e.printStackTrace() yang ada di filter lama ikut terhapus (sesuai S2-T10)
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfigurerAdapter
        extends org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter {

    @Autowired
    private StatelessJwtAuthenticationFilter statelessJwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable()
            .httpBasic().disable()
            .authorizeRequests()
                // Path publik yang tidak memerlukan autentikasi
                // Path ini juga dikonfigurasi di auth.public-paths agar shouldNotFilter() aktif
                .antMatchers("/v2/api-docs", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            .and()
            .exceptionHandling();

        // Ganti AuthenticationFilter lama dengan StatelessJwtAuthenticationFilter dari auth-commons
        http.addFilterBefore(statelessJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
