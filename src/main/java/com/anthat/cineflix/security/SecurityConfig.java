package com.anthat.cineflix.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public AuthenticationProvider authenticationProvider(@Qualifier("userService") UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.formLogin(withDefaults());
        return http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/v1/user/register", "/api/v1/user/login").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User.withUsername("user").password(passwordEncoder().encode("user")).roles("USER").build();
//        UserDetails creator = User.withUsername("creator").password(passwordEncoder().encode("creator")).roles("CREATOR").build();
//        UserDetails admin = User.withUsername("admin").password(passwordEncoder().encode("admin")).roles("ADMIN").build();
//
//        return new InMemoryUserDetailsManager(user, creator, admin);
//        UserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
//        userDetailsManager.createUser(user);
//        userDetailsManager.createUser(creator);
//        userDetailsManager.createUser(admin);
//
//        return userDetailsManager;
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // This one is required to authenticate user before generating token
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
