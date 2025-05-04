package com.cheeza.Cheeza.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.web.servlet.function.RequestPredicates.headers;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // private final UserDetailsService userDetailsService;

    // Remove PasswordEncoder from constructor and define separately
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/orders/place").authenticated()
            .requestMatchers("/auth/userlist").hasRole("ADMIN")
            .requestMatchers("/auth/profile","/auth/profile/edit").authenticated()
                            .requestMatchers("/ws/**").permitAll()
                            .requestMatchers("/admin/**","/uploads/**").permitAll()
                .requestMatchers(
                        "/",
                        "/auth/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/auth/register",
                        "/menu",
                        "/h2-console/**"
                ).permitAll()
                .anyRequest().authenticated()
                             // Remove later
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .permitAll()
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .permitAll()
                .logoutSuccessUrl("/")
            ).headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**"
                                , "/h2-console/**"
                                ,"/api/orders/place"
                                ,"/auth/login",
                                "/auth/register")
                );
        
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/error");
    }


    // @Bean
    // public DaoAuthenticationProvider authenticationProvider() {
    //     DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    //     authProvider.setUserDetailsService(userDetailsService);
    //     authProvider.setPasswordEncoder(passwordEncoder()); // Call the method directly
    //     return authProvider;
    // }


}