package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.service.RefactoringTodoService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class RefactoringDoneFormat {

    private RefactoringDoneFormat(Long refactoringTodoId, Member member, String code, String description) {
        this.refactoringTodoId = refactoringTodoId;
        this.member = member;
        this.code = code;
        this.description = description;
    }

    @Autowired
    private RefactoringTodoService refactoringTodoService;

    private Long refactoringTodoId;
    private Member member;
    private String code;
    private String description;

    public static RefactoringDoneFormat of(Long refactoringTodoId, Member member, String code, String description) {
        return new RefactoringDoneFormat(refactoringTodoId, member, code, description);
    }

    public static RefactoringDone toEntity(RefactoringTodo refactoringTodo, RefactoringDoneFormat dto) {
        return new RefactoringDone(
                null,
                refactoringTodo,
                dto.member,
                dto.code,
                dto.description
        );
    }
}
