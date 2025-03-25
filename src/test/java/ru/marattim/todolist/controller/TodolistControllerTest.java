package ru.marattim.todolist.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.jooq.DSLContext;
import org.jooq.generated.tables.records.TodolistRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import ru.marattim.todolist.controller.dto.CreateTodo;
import ru.marattim.todolist.controller.dto.TodoInfo;
import ru.marattim.todolist.tests_infrastructure.IntegrationTest;
import ru.marattim.todolist.tests_infrastructure.TestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.generated.Tables.TODOLIST;
import static org.jooq.generated.Tables.USERS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.marattim.todolist.tests_infrastructure.TestUtils.json;

@IntegrationTest
class TodolistControllerTest {
    @Autowired
    private DSLContext dsl;
    @Autowired
    private TestUtils testUtils;

    @Test
    @WithUserDetails("test")
    void shouldReturnAll() {
        dsl.insertInto(USERS, USERS.USERNAME, USERS.PASSWORD, USERS.ENABLED)
            .values("test", "qwerty", true)
            .values("killer", "qwerty1", true)
            .execute();
        dsl.insertInto(TODOLIST,
                TODOLIST.TITLE, TODOLIST.TODO, TODOLIST.IS_COMPLETED, TODOLIST.USERNAME)
            .values("ПИПО", "Сделать дз по Промышленной инженерии ПО", true, "test")
            .values("SRE", "Прорешать тестики по SRE", false, "test")
            .values("Уничтожение", "Уничтожить преподов ставящих меньше 4", false, "killer")
            .execute();

        var todoInfoList = testUtils.query(get("/todo/all"))
            .andExpect(status().isOk())
            .convertTo(new TypeReference<List<TodoInfo>>() {
            });

        assertThat(todoInfoList)
            .usingRecursiveFieldByFieldElementComparator(
                RecursiveComparisonConfiguration.builder()
                    .withComparedFields("title", "todo", "isCompleted")
                    .build())
            .containsOnly(
                TodoInfo.builder()
                    .title("ПИПО")
                    .todo("Сделать дз по Промышленной инженерии ПО")
                    .isCompleted(true)
                    .build(),
                TodoInfo.builder()
                    .title("SRE")
                    .todo("Прорешать тестики по SRE")
                    .isCompleted(false)
                    .build()
            );
    }

    @Test
    @WithUserDetails("test")
    void shouldReturnOne() {
        dsl.insertInto(USERS, USERS.USERNAME, USERS.PASSWORD, USERS.ENABLED)
            .values("test", "qwerty", true)
            .execute();
        dsl.insertInto(TODOLIST,
                TODOLIST.ID, TODOLIST.TITLE, TODOLIST.TODO, TODOLIST.IS_COMPLETED,
                TODOLIST.USERNAME)
            .values(1, "ПИПО", "Сделать дз по Промышленной инженерии ПО", true, "test")
            .execute();

        TodoInfo response = testUtils.query(get("/todo")
                .param("id", "1"))
            .andExpect(status().isOk())
            .convertTo(TodoInfo.class);

        assertThat(response)
            .usingRecursiveComparison()
            .comparingOnlyFields("title", "todo", "isCompleted")
            .isEqualTo(TodoInfo.builder()
                .title("ПИПО")
                .todo("Сделать дз по Промышленной инженерии ПО")
                .isCompleted(true)
                .build());
    }

    @Test
    @WithUserDetails("test")
    void shouldCreateTodo() {
        dsl.insertInto(USERS, USERS.USERNAME, USERS.PASSWORD, USERS.ENABLED)
            .values("test", "qwerty", true)
            .execute();

        Integer id = testUtils.query(post("/todo")
                .content(json(
                    CreateTodo.builder()
                        .title("ПИПО")
                        .todo("Сделать дз по Промышленной инженерии ПО")
                        .build())))
            .andExpect(status().isOk())
            .convertTo(Integer.class);

        TodolistRecord createdTodo = dsl.selectFrom(TODOLIST)
            .where(TODOLIST.ID.eq(id))
            .fetchOne();

        assertThat(createdTodo)
            .satisfies(todo -> {
                assertThat(todo.getTitle()).isEqualTo("ПИПО");
                assertThat(todo.getTodo()).isEqualTo("Сделать дз по Промышленной инженерии ПО");
                assertThat(todo.getIsCompleted()).isFalse();
                assertThat(todo.getUsername()).isEqualTo("test");
            });
    }

    @Test
    @WithUserDetails("test")
    void shouldCorrectlyDelete() {
        dsl.insertInto(USERS, USERS.USERNAME, USERS.PASSWORD, USERS.ENABLED)
            .values("test", "qwerty", true)
            .execute();
        dsl.insertInto(TODOLIST,
                TODOLIST.ID, TODOLIST.TITLE, TODOLIST.TODO, TODOLIST.IS_COMPLETED, TODOLIST.USERNAME)
            .values(1, "ПИПО", "Сделать дз по Промышленной инженерии ПО", true, "test")
            .execute();

        testUtils.query(delete("/todo")
                .param("id", "1"))
            .andExpect(status().isOk());

        TodolistRecord todo = dsl.selectFrom(TODOLIST)
            .where(TODOLIST.ID.eq(1))
            .fetchOne();

        assertThat(todo).isNull();
    }

    @Test
    @WithUserDetails("test")
    void shouldChangeStatus() {
        dsl.insertInto(USERS, USERS.USERNAME, USERS.PASSWORD, USERS.ENABLED)
            .values("test", "qwerty", true)
            .execute();
        dsl.insertInto(TODOLIST,
                TODOLIST.ID, TODOLIST.TITLE, TODOLIST.TODO, TODOLIST.IS_COMPLETED, TODOLIST.USERNAME)
            .values(1, "ПИПО", "Сделать дз по Промышленной инженерии ПО", false, "test")
            .execute();

        testUtils.query(patch("/todo")
                .param("id", "1"))
            .andExpect(status().isOk());

        TodolistRecord todo = dsl.selectFrom(TODOLIST)
            .where(TODOLIST.ID.eq(1))
            .fetchOne();

        assertThat(todo.getIsCompleted()).isTrue();
    }
}
