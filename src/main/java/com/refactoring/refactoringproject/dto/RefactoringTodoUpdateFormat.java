package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Member;
import lombok.Getter;

import java.util.List;

@Getter
public class RefactoringTodoUpdateFormat {

    public RefactoringTodoUpdateFormat(Long refactoringTodoId, Member member, String language, String code, String description, List<RefactoringTodoOrderFormat> todoOrderFormat) {
        this.refactoringTodoId = refactoringTodoId;
        this.member = member; // TODO: 지금은 외부에서 주입받아 설정하지만, 스프링 시큐리티를 적용하면 SecurityContextHolder에서 꺼내온 객체를 사용해야 한다.
        this.language = language;
        this.code = code;
        this.description = description;
        this.todoOrderFormat = todoOrderFormat;
    }

    private Long refactoringTodoId;
    private Member member; // TODO: 지금은 외부에서 주입받아 설정하지만, 스프링 시큐리티를 적용하면 SecurityContextHolder에서 꺼내온 객체를 사용해야 한다.
    private String language;
    private String code;
    private String description;
    private List<RefactoringTodoOrderFormat> todoOrderFormat;

    public static RefactoringTodoUpdateFormat of(Long refactoringTodoId, Member member, String language, String code, String description, List<RefactoringTodoOrderFormat> todoOrderFormat) {
        return new RefactoringTodoUpdateFormat(refactoringTodoId, member, language, code, description, todoOrderFormat);
    }
}
