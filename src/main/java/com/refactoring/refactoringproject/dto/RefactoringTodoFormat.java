package com.refactoring.refactoringproject.dto;

import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;
import com.refactoring.refactoringproject.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RefactoringTodoFormat {

    public RefactoringTodoFormat() {
    }

    private RefactoringTodoFormat(Member member, String language, String code, String description, List<RefactoringTodoOrderFormat> orderFormats) {
        this.member = member; //TODO: 지금은 외부에서 주입받아 설정하지만, 스프링 시큐리티를 적용하면 SecurityContextHolder에서 꺼내온 객체를 사용해야 한다.
        this.language = language;
        this.code = code;
        this.description = description;
        this.orderFormats = orderFormats;
    }

    private Member member;
    private String language;
    private String code;
    private String description;
    private List<RefactoringTodoOrderFormat> orderFormats;

    public static RefactoringTodoFormat of(Member member, String language, String code, String description, List<RefactoringTodoOrderFormat> orderFormats) {
        return new RefactoringTodoFormat(
                member,
                language,
                code,
                description,
                orderFormats
        );
    }

    public static RefactoringTodo toEntity(RefactoringTodoFormat dto) {
        RefactoringTodo refactoringTodo = new RefactoringTodo(dto.member, dto.language, dto.code, dto.description);
        List<RefactoringTodoOrder> orders = dto.orderFormats.stream().map(format -> RefactoringTodoOrderFormat.toEntity(format))
                .collect(Collectors.toList());
        orders.forEach(order -> refactoringTodo.addRefactoringOrder(order));

        return refactoringTodo;
    }

}
