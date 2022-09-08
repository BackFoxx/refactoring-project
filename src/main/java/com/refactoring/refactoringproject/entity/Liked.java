package com.refactoring.refactoringproject.entity;

import javax.persistence.*;

@Entity
public class Liked {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "REFACTORING_DONE_ID", nullable = false)
    private RefactoringDone refactoringDone;
}
