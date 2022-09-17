package com.refactoring.refactoringproject.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;
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

    @QueryProjection
    public RefactoringTodoResponse(Long id, String language, String code, String description, Long favoriteCount, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.language = language;
        this.code = code;
        this.description = description;
        this.favoriteCount = favoriteCount;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static RefactoringTodoResponse of(Long id, Member member, String language, String code, String description, RefactoringDone bestPractice, List<RefactoringTodoOrderResponse> orders, int favoriteCount, LocalDateTime createdAt, LocalDateTime modifiedAt) {
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

    private Long favoriteCount;
    // 해당 RefactoringTodo를 즐겨찾기 한 사용자의 숫자

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
