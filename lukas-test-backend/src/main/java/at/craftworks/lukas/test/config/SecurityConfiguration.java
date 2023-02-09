/*
 * Copyright (c) 2014-2022 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2022-07-20
 */
package at.craftworks.lukas.test.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.List;
import java.util.UUID;

@Configuration
@Slf4j
@EnableWebSecurity
@Profile("!test && !e2e")
public class SecurityConfiguration extends BaseSecurityConfiguration {


    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {

        // OpenID Connect configuration
        super.configureHttpSecurity(http)
                // sets up application as a resource server
                // it will check if there is an access token on every request and whether it is valid or not
                .oauth2ResourceServer()
                // this will be checked against JWT access token
                .jwt();

        return http.build();
    }

}
