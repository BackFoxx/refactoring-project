package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;
import lombok.Getter;

@Getter
public class RefactoringTodoOrderResponse {
    private String content;

    private RefactoringTodoOrderResponse(String content) {
        this.content = content;
    }

    public static RefactoringTodoOrderResponse from(RefactoringTodoOrder entity) {
        return new RefactoringTodoOrderResponse(entity.getContent());
    }
}
