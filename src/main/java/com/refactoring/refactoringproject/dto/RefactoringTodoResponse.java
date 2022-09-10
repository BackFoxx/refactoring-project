package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RefactoringTodoResponse {
    public RefactoringTodoResponse(Long id, Member member, String language, String code, String description, RefactoringDone bestPractice, List<RefactoringTodoOrderResponse> orders) {
        this.id = id;
        this.member = member;
        this.language = language;
        this.code = code;
        this.description = description;
        this.bestPractice = bestPractice;
        this.orders = orders;
    }

    public static RefactoringTodoResponse of(Long id, Member member, String language, String code, String description, RefactoringDone bestPractice, List<RefactoringTodoOrderResponse> orders) {
        return new RefactoringTodoResponse(id, member, language, code, description, bestPractice, orders);
    }

    public static RefactoringTodoResponse from(RefactoringTodo entity) {
        return new RefactoringTodoResponse(
                entity.getId(),
                entity.getMember(),
                entity.getLanguage(),
                entity.getCode(),
                entity.getDescription(),
                entity.getBestPractice(),
                entity.getOrders().stream()
                        .map(order -> RefactoringTodoOrderResponse.from(order))
                        .collect(Collectors.toList())
        );
    }

    private Long id;
    private Member member;
    private String language;
    private String code;
    private String description;
    private RefactoringDone bestPractice;
    private List<RefactoringTodoOrderResponse> orders;
}
