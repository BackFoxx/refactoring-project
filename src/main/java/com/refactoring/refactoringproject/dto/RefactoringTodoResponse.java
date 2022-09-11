package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString(exclude = {"member", "bestPractice", "code", "orders"})
public class RefactoringTodoResponse {
    private RefactoringTodoResponse(Long id, Member member, String language, String code, String description, RefactoringDone bestPractice, List<RefactoringTodoOrderResponse> orders, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.member = member;
        this.language = language;
        this.code = code;
        this.description = description;
        this.bestPractice = bestPractice;
        this.orders = orders;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static RefactoringTodoResponse of(Long id, Member member, String language, String code, String description, RefactoringDone bestPractice, List<RefactoringTodoOrderResponse> orders, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        return new RefactoringTodoResponse(id, member, language, code, description, bestPractice, orders, createdAt, modifiedAt);
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
                        .collect(Collectors.toList()),
                entity.getCreatedAt(),
                entity.getModifiedAt()
        );
    }

    private Long id;
    private Member member;
    private String language;
    private String code;
    private String description;
    private RefactoringDone bestPractice;
    private List<RefactoringTodoOrderResponse> orders;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
