package com.tcsion.eforms.config;

import com.tcsion.eforms.entity.Role;
import com.tcsion.eforms.security.CustomAccessDeniedHandler;
import com.tcsion.eforms.security.CustomAuthenticationFailureHandler;
import com.tcsion.eforms.security.CustomAuthenticationSuccessHandler;
import com.tcsion.eforms.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public SessionRegistryImpl sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/login", "/forgot-password", "/reset-password", "/error",
                              "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()
                .antMatchers("/access-denied").permitAll()
                .antMatchers("/user-management/**").hasAnyRole(Role.TEAM_LEAD, Role.SYSTEM_ADMIN)
                .antMatchers("/audit-logs/**").hasAnyRole(Role.TEAM_LEAD, Role.SYSTEM_ADMIN)
                .antMatchers("/master-data/**").hasAnyRole(Role.TEAM_LEAD, Role.TECHNICAL_LEAD, Role.DASHBOARD_HANDLER)
                .antMatchers("/import/**").hasAnyRole(Role.TEAM_LEAD, Role.TECHNICAL_LEAD, Role.DASHBOARD_HANDLER)
                .antMatchers("/executive-command-center/**")
                    .hasAnyRole(Role.TEAM_LEAD, Role.TECHNICAL_LEAD, Role.DASHBOARD_HANDLER)
                .antMatchers("/tickets/all").hasAnyRole(Role.TEAM_LEAD, Role.TECHNICAL_LEAD, Role.DASHBOARD_HANDLER)
                .antMatchers("/api/admin/**").hasAnyRole(Role.TEAM_LEAD, Role.SYSTEM_ADMIN)
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/perform-login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            .and()
            .sessionManagement()
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
                .sessionRegistry(sessionRegistry())
            .and()
            .and()
            .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
            .and()
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .headers()
                .contentTypeOptions().and()
                .frameOptions().sameOrigin().and()
                .httpStrictTransportSecurity()
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                    .and()
                .addHeaderWriter((request, response) -> {
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                    response.setHeader("Referrer-Policy", "same-origin");
                    response.setHeader("Content-Security-Policy",
                            "default-src 'self'; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                            "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                            "img-src 'self' data:; font-src 'self' https://cdn.jsdelivr.net;");
                });
    }
}
