package com.karpov.ru.foodboxd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности приложения.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Определяет цепочку фильтров безопасности.
     * @param http объект конфигурации HttpSecurity
     * @return построенная цепочка фильтров
     * @throws Exception при ошибках конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/rate/**", "/lists/*/add", "/lists/*/remove/**", "/lists/*/reorder")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers("/", "/restaurants/**", "/menu-item/**", "/reviews/**",
                                "/register", "/login", "/forgot-password", "/reset-password").permitAll()
                        .requestMatchers("/admin/**", "/restaurants/*/edit", "/restaurants/*/deactivate",
                                "/restaurants/*/photos/**", "/menu-item/*/hide",
                                "/menu-item/*/photos/**").hasRole("ADMIN")
                        .requestMatchers("/profile/**", "/lists/**", "/rate/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }

    /**
     * Предоставляет кодировщик паролей BCrypt.
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}