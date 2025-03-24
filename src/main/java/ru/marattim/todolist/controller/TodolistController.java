package ru.marattim.todolist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.generated.tables.records.TodolistRecord;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.marattim.todolist.controller.dto.CreateTodo;
import ru.marattim.todolist.controller.dto.TodoInfo;
import static org.jooq.generated.Tables.TODOLIST;

@RestController
@AllArgsConstructor
@RequestMapping("todo")
public class TodolistController {
    private final DSLContext dsl;

    @GetMapping("all")
    public List<TodoInfo> all(@Parameter(hidden = true)
                              @AuthenticationPrincipal(errorOnInvalidType = true)
                              UserDetails userDetails) {
        return dsl.selectFrom(TODOLIST)
            .where(TODOLIST.USERNAME.eq(userDetails.getUsername()))
            .fetchInto(TodolistRecord.class)
            .stream()
            .map(rec -> TodoInfo.builder()
                .id(rec.getId())
                .title(rec.getTitle())
                .todo(rec.getTodo())
                .isCompleted(rec.getIsCompleted())
                .createdAt(rec.getCreatedAt().toLocalDateTime())
                .build())
            .toList();
    }

    @PostMapping
    public Integer create(@RequestBody
                          CreateTodo createTodo,
                          @Parameter(hidden = true)
                          @AuthenticationPrincipal(errorOnInvalidType = true)
                          UserDetails userDetails) {
        return dsl.insertInto(TODOLIST, TODOLIST.TITLE, TODOLIST.TODO, TODOLIST.IS_COMPLETED,
                TODOLIST.USERNAME)
            .values(createTodo.getTitle(), createTodo.getTodo(), false, userDetails.getUsername())
            .returning(TODOLIST.ID)
            .fetchOne()
            .getId();
    }

    @GetMapping
    public TodoInfo read(@RequestParam Integer id,
                         @Parameter(hidden = true)
                         @AuthenticationPrincipal(errorOnInvalidType = true)
                         UserDetails userDetails) {
        var todo = dsl.selectFrom(TODOLIST)
            .where(TODOLIST.ID.eq(id).and(TODOLIST.USERNAME.eq(userDetails.getUsername())))
            .fetchOne();

        if (todo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo с таким id не найдено");
        }

        return TodoInfo.builder()
            .id(todo.getId())
            .title(todo.getTitle())
            .todo(todo.getTodo())
            .createdAt(todo.getCreatedAt().toLocalDateTime())
            .isCompleted(todo.getIsCompleted())
            .build();
    }

    @DeleteMapping
    public void delete(@RequestParam Integer id,
                       @Parameter(hidden = true)
                       @AuthenticationPrincipal(errorOnInvalidType = true)
                       UserDetails userDetails) {
        dsl.deleteFrom(TODOLIST)
            .where(TODOLIST.ID.eq(id).and(TODOLIST.USERNAME.eq(userDetails.getUsername())))
            .execute();
    }

    @Operation(summary = "Меняет статус задачи на выполненную")
    @PatchMapping
    public void update(@RequestParam Integer id,
                       @Parameter(hidden = true)
                       @AuthenticationPrincipal(errorOnInvalidType = true)
                       UserDetails userDetails) {
        int updatedCount = dsl.update(TODOLIST)
            .set(TODOLIST.IS_COMPLETED, true)
            .where(TODOLIST.ID.eq(id).and(TODOLIST.USERNAME.eq(userDetails.getUsername())))
            .execute();

        if (updatedCount == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo с таким id не найдено");
        }
    }


}
