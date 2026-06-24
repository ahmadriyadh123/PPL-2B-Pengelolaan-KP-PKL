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
 * S3-T05 — Migrasi company-service ke auth-commons.
 *
 * Perubahan dari konfigurasi lama:
 * - AuthenticationFilter lama (~148 LOC, HTTP call ke /account/verify) dihapus
 * - StatelessJwtAuthenticationFilter dari auth-commons digunakan sebagai pengganti
 * - Public paths dikonfigurasi di auth.public-paths (application.properties)
 *   DAN di antMatchers agar konsisten antara shouldNotFilter() dan Spring Security
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
                .antMatchers("/v2/api-docs", "/actuator/health").permitAll()
                .antMatchers("/company/criteria").permitAll()
                .antMatchers("/company/submission/create").permitAll()
                .anyRequest().authenticated()
            .and()
            .exceptionHandling();

        http.addFilterBefore(statelessJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}