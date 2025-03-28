package ru.marattim.todolist.controller.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoInfo {
    private Integer id;
    private String title;
    private String todo;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
}
