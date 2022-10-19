package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import lombok.Getter;

@Getter
public class RefactoringDoneResponse {
    public RefactoringDoneResponse(Long id, Member member, RefactoringTodoResponse refactoringTodoResponse, String code, String description) {
        this.id = id;
        this.member = member;
        this.refactoringTodoResponse = refactoringTodoResponse;
        this.code = code;
        this.description = description;
    }

    private Long id;
    private Member member; // TODO: 엔티티가 컨트롤러까지 넘어가는 것이 적합하지 않기 때문에 DTO를 사용중인데 필드로 ENTITY가 들어간다. API를 어떻게 구성할 지 고려하여 추후 수정해야 한다.
    private RefactoringTodoResponse refactoringTodoResponse;
    private String code;
    private String description;

    public static RefactoringDoneResponse from(RefactoringDone entity) {
        return new RefactoringDoneResponse(
                entity.getId(),
                entity.getMember(),
                RefactoringTodoResponse.from(entity.getRefactoringTodo()),
                entity.getCode(),
                entity.getDescription()
        );
    }
}
