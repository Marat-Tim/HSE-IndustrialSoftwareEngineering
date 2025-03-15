package ru.marattim.todolist.tests_infrastructure;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;

public class ConvertableResultActions {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }

    private ResultActions resultActions;

    ConvertableResultActions(ResultActions resultActions) {
        this.resultActions = resultActions;
    }

    public ConvertableResultActions andExpect(ResultMatcher matcher) {
        try {
            resultActions = resultActions.andExpect(matcher);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ConvertableResultActions andDo(ResultHandler handler) {
        try {
            resultActions = resultActions.andDo(handler);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MvcResult andReturn() {
        return resultActions.andReturn();
    }

    public ConvertableResultActions andExpectAll(ResultMatcher... matchers) {
        try {
            resultActions = resultActions.andExpectAll(matchers);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T convertTo(Class<T> clazz) {
        try {
            return MAPPER.readValue(andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), clazz);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T convertTo(TypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), typeReference);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
