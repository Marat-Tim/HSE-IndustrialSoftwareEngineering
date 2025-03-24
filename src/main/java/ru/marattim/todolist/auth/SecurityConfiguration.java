package ru.marattim.todolist.auth;

import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.servlet.LogbookFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@AllArgsConstructor
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           @Qualifier("logbookForIncomingRequests") Logbook logbook) throws Exception {
        httpSecurity
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/register/**", "/confirm/**", "/login").permitAll()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(form -> form
                    .successHandler((rq, rs, auth) -> {
                            log.info("Аутентификация успешна: {}", auth.getName());
                            rs.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8).toString());
                            rs.getWriter().write("\"Аутентификация успешна\"");
                    })
                    .failureHandler((rq, rs, auth) -> {
                            log.info("Ошибка аутентификации", auth);
                            rs.sendError(401);
                            rs.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8).toString());
                            rs.getWriter().write("\"Пользователя с такими данными не найдено\"");
                    })
                )
                .logout(logout -> logout
                    .logoutSuccessHandler((rq, rs, auth) -> {
                        rs.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8).toString());
                        rs.getWriter().write("\"Вы успешно разлогинились\"");
                    }))
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .addFilterAfter(
                        new LogbookFilter(logbook),
                        BasicAuthenticationFilter.class
                );

        return httpSecurity.build();
    }

    @Bean
    public JdbcUserDetailsManager customUserDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public PasswordEncoder customPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
