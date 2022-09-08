package com.refactoring.refactoringproject.entity;

import javax.persistence.*;

@Entity
public class RefactoringDone {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "REFACTORING_TODO_ID", nullable = false)
    private RefactoringTodo refactoringTodo;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(length = 10000, nullable = false)
    private String code;

    @Column(length = 1000)
    private String description;
}
