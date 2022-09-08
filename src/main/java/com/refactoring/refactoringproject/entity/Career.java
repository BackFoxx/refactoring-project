package com.refactoring.refactoringproject.entity;

import javax.persistence.*;

@Entity
@Table(name = "CAREER")
public class Career extends BaseEntityTime {
    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(name = "COMPANY", length = 50, nullable = false)
    private String company;

    @Column(name = "MONTHS", nullable = false)
    private int months;
}
