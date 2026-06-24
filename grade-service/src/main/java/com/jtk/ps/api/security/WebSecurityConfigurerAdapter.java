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
 * S3-T06 — Migrasi grade-service ke auth-commons.
 *
 * Perubahan dari konfigurasi lama:
 * - AuthenticationFilter lama (~138 LOC, HTTP call ke /account/verify) dihapus
 * - StatelessJwtAuthenticationFilter dari auth-commons digunakan sebagai pengganti
 * - Rule hasAnyAuthority untuk /seminar dan /courses DIPERTAHANKAN persis dari lama
 * - SecurityContextLogoutHandler yang ada di filter lama ikut terhapus (sesuai S2-T08)
 * - e.printStackTrace() yang ada di filter lama ikut terhapus (sesuai S2-T10)
 * - KafkaConsumer tidak disentuh — tidak ada JWT manual di sana
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
                // Role-based rules dipertahankan dari konfigurasi lama
                .antMatchers("/seminar").hasAnyAuthority("COMMITTEE", "HEAD_STUDY_PROGRAM")
                .antMatchers("/courses").hasAnyAuthority("COMMITTEE", "HEAD_STUDY_PROGRAM")
                .anyRequest().authenticated()
            .and()
            .exceptionHandling();

        http.addFilterBefore(statelessJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}