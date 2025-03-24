package ru.marattim.todolist.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateTodo {
    private String title;
    private String todo;
}
