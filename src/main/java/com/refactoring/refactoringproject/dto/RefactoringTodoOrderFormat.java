package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;

public class RefactoringTodoOrderFormat {
    private RefactoringTodoOrderFormat(String content) {
        this.content = content;
    }

    private String content;

    public static RefactoringTodoOrderFormat of(String content) {
        return new RefactoringTodoOrderFormat(content);
    }

    public static RefactoringTodoOrder toEntity(RefactoringTodoOrderFormat dto) {
        return new RefactoringTodoOrder(null, null, dto.content);
    }
}
