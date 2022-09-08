package com.refactoring.refactoringproject.entity;

import javax.persistence.*;

@Entity
public class RefactoringTodoOrder extends BaseEntityTime {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "REFACTORING_TODO_ID")
    private RefactoringTodo refactoringTodo;

    @Column(length = 200, nullable = false)
    private String content;
}
