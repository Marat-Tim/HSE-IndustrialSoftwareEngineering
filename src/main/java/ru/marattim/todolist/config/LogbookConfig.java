package ru.marattim.todolist.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;
import org.zalando.logbook.core.ChunkingSink;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.zalando.logbook.core.DefaultSink;

import static org.zalando.logbook.core.Conditions.contentType;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;

@Configuration
@AllArgsConstructor
public class LogbookConfig {
    @Value("${server.servlet.context-path}")
    private final String contextPath;

    @Bean
    public Logbook logbookForIncomingRequests() {

        return Logbook.builder()
                .condition(
                        exclude(
                                requestTo(contextPath + "/actuator/**"),
                                requestTo(contextPath + "/swagger-ui/**"),
                                requestTo(contextPath + "/v3/api-docs/**"),
                                contentType("application/octet-stream")
                        )
                )
                .headerFilter(headers -> {
                    List<String> names = new ArrayList<>(headers.keySet());
                    return headers.delete(names);
                })
                .attributeExtractor(new AttributeExtractor() {

                    @Override
                    public HttpAttributes extract(HttpRequest request) {
                        return HttpAttributes.of("User", getUsernameOpt().orElse(null));
                    }
                })
                .sink(new ChunkingSink(new DefaultSink(new DefaultHttpLogFormatter(), new DefaultHttpLogWriter()), 10_000))
                .build();
    }

    private Optional<String> getUsernameOpt() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(auth -> ((UserDetails) auth.getPrincipal()))
                .map(UserDetails::getUsername);
    }

}
