package ru.marattim.todolist.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("register")
public class RegisterController {
    private final JavaMailSender mailSender;
    @Value("${todolist.mail.username}")
    private final String ourEmail;
    @Value("${todolist.confirm-url}")
    private final String confirmUrl;
    private final JdbcUserDetailsManager jdbcUserDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final ConcurrentMap<UUID, UserPassword> tokenToUserPassword = new ConcurrentHashMap<>();

    @GetMapping("init/email")
    public String initRegistrationUsingEmail(@RequestParam String email, @RequestParam String password) throws MessagingException, IOException {
        log.info("Используем регистрацию через email");
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        Resource resource = new ClassPathResource("templates/registration.html");
        UUID token = UUID.randomUUID();
        String htmlMsg = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8)
                .replace("{{token}}", token.toString())
                .replace("{{url}}", confirmUrl);
        tokenToUserPassword.put(token, new UserPassword(email, password));
        helper.setText(htmlMsg, true);
        helper.setTo(email);
        helper.setSubject("Подтверждение регистрации");
        helper.setFrom(ourEmail);
        mailSender.send(mimeMessage);
        return "Проверьте свою почту";
    }

    @GetMapping("init/log")
    public String initRegistrationUsingLog(@RequestParam String email, @RequestParam String password) {
        log.info("Используем регистрацию через лог");
        UUID token = UUID.randomUUID();
        tokenToUserPassword.put(token, new UserPassword(email, password));
        log.info("Для подтверждения регистрации {} используйте id = {}", email, token);
        return "Проверьте лог";
    }

    @PostMapping(value = "confirm/email", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String confirmRegistrationUsingEmail(@RequestParam UUID token) {
        return confirmRegistration(token);
    }

    @PostMapping(value = "confirm/log")
    public String confirmRegistrationUsingLog(@RequestParam UUID token) {
        return confirmRegistration(token);
    }

    private String confirmRegistration(UUID token) {
        var userPassword = tokenToUserPassword.remove(token);
        if (userPassword != null) {
            jdbcUserDetailsManager.createUser(
                    new User(
                            userPassword.user(),
                            passwordEncoder.encode(userPassword.password()),
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    )
            );
            return "Регистрация подтверждена";
        } else {
            return "Токен невалидный";
        }
    }

    record UserPassword(String user, String password) {
    }

}
