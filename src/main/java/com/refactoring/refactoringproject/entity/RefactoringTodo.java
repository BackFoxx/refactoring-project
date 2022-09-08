package com.refactoring.refactoringproject.entity;

import javax.persistence.*;

@Entity
public class RefactoringTodo extends BaseEntityTime {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(length = 10, nullable = false)
    private String language;

    @Column(length = 10000, nullable = false)
    private String code;

    @Column(length = 1000)
    private String description;

    @OneToOne
    @JoinColumn(name = "BEST_PRACTICE")
    private RefactoringDone bestPractice;
}
