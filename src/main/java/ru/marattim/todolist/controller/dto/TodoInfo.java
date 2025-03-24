package ru.marattim.todolist.controller.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoInfo {
    private Integer id;
    private String title;
    private String todo;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
}
