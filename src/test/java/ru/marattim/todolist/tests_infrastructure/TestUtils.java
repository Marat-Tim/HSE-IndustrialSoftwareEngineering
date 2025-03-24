package ru.marattim.todolist.tests_infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Component
@AllArgsConstructor
public class TestUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    public String getResourceText(String path) {
        try (var inputStream = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + path)
            .getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getObjectFromResource(Class<T> clazz, String path) {
        try {
            return objectMapper.readValue(getResourceText(path), clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getObjectFromResource(TypeReference<T> typeReference, String path) {
        try {
            return objectMapper.readValue(getResourceText(path), typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> getObjectFromResource(Class<T> clazz, String... paths) {
        return Arrays.stream(paths)
            .map(path -> getObjectFromResource(clazz, path))
            .toList();
    }

    public <T> T getResponseDto(ResultActions resultActions, Class<T> clazz) {
        try {
            String contentString = resultActions.andReturn().getResponse().getContentAsString();

            return objectMapper.readValue(contentString, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getResponseDto(ResultActions resultActions, TypeReference<T> typeReference) {
        try {
            String contentString = resultActions.andReturn().getResponse().getContentAsString();

            return objectMapper.readValue(contentString, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConvertableResultActions query(MockHttpServletRequestBuilder requestBuilder) {
        try {
            return new ConvertableResultActions(mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String json(T body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
