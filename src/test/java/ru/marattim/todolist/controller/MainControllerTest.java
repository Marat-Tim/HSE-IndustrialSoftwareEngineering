package ru.marattim.todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import ru.marattim.todolist.tests_infrastructure.IntegrationTest;
import ru.marattim.todolist.tests_infrastructure.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class MainControllerTest {
    @Autowired
    private TestUtils testUtils;

    @Test
    @WithUserDetails("test")
    void shouldReturnHelloWorld() {
        String responseBody = testUtils.query(get("/test"))
            .andExpect(status().isOk())
            .convertTo(String.class);

        assertEquals("Hello world", responseBody);
    }
}
