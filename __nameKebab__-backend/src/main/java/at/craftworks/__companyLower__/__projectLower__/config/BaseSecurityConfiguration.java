/*
 * Copyright (c) 2014-2022 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2022-07-20
 */
package at.craftworks.__companyLower__.__projectLower__.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class BaseSecurityConfiguration {

    @Value("${monitoring.actuator.password:''}")
    private String actuatorPassword;

    @Bean
    public static BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(false);
        return loggingFilter;
    }

    public HttpSecurity configureHttpSecurity(HttpSecurity http) throws Exception {

        http
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // disable csrf - OK if bearer token is used and stored in local storage
                // https://security.stackexchange.com/questions/170388/do-i-need-csrf-token-if-im-using-bearer-jwt
                .csrf(AbstractHttpConfigurer::disable)
                .headers(c -> c.frameOptions()
                        .sameOrigin()
                        .cacheControl())

                // allow full access to swagger (which should be enable in dev mode only)
                .authorizeHttpRequests(auth ->
                        auth.antMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                )

                .authorizeHttpRequests(authz -> authz
                        .antMatchers("/api/v1/auth/**").permitAll()
                        //.antMatchers("/api/public/**").permitAll()
                        .antMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyRequest().authenticated()
                );

       return http;
    }

    private InMemoryUserDetailsManager createActuatorUserDetailsService() {
        var pwdEncoder = bCryptPasswordEncoder();
        String pwd = actuatorPassword;
        if (pwd == null || pwd.length() < 10) {
            log.warn("Setting Actuator password to random value because of insufficient password length");
            pwd = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        }
        UserDetails user = new User(
                "actuator",
                pwdEncoder.encode(pwd),
                List.of(new SimpleGrantedAuthority("ACTUATOR")));
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Filter chain that configures access to the /actuator endpoints
     * This filter chain should is annoated with order 1, which means it is applied
     * before the default filter chain.
     * <p>
     * It should be applied before, because the root <code>antMatcher(/actuator/**)</code>
     * applies this chain only for actuator requests
     * </p>
     */
    @Order(1)
    @Bean
    public SecurityFilterChain filterChainActuator(HttpSecurity http) throws Exception {

        http
                .antMatcher("/actuator/**")
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // add basic auth access to actuator endpoint
                .authorizeHttpRequests(auth ->
                        auth.antMatchers("/actuator/**").hasAuthority("ACTUATOR")
                )
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(createActuatorUserDetailsService());

        return http.build();
    }
}