package com.refactoring.refactoringproject.entity;

import javax.persistence.*;

@Entity
public class Favorite extends BaseEntityTime {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "REFACTORING_TODO_ID", nullable = false)
    private RefactoringTodo refactoringTodo;
}
