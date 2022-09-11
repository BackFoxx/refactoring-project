package com.refactoring.refactoringproject.entity;

import lombok.Getter;
import org.springframework.data.util.Lazy;

import javax.persistence.*;

@Entity
@Getter
public class Favorite extends BaseEntityTime {
    public Favorite() {
    }

    public Favorite(Member member, RefactoringTodo refactoringTodo) {
        this.member = member;
        this.refactoringTodo = refactoringTodo;
    }

    public static Favorite of(Member member, RefactoringTodo refactoringTodo) {
        return new Favorite(member, refactoringTodo);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REFACTORING_TODO_ID", nullable = false)
    private RefactoringTodo refactoringTodo;
}
