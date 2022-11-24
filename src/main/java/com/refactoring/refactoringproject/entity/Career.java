package com.refactoring.refactoringproject.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "CAREER")
public class Career extends BaseEntityTime {

    public static final String CHANGING_MEMBER_MESSAGE = "You can't change Member of Career which is already assigned.";

    public Career() {
    }

    public Career(Long id, Member member, String company, int months) {
        this.id = id;
        this.member = member;
        this.company = company;
        this.months = months;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(name = "COMPANY", length = 50, nullable = false)
    private String company;

    @Column(name = "MONTHS", nullable = false)
    private int months;

    public void assignMember(Member member) {
        if (this.member != null) {
            throw new IllegalArgumentException(CHANGING_MEMBER_MESSAGE);
        }

        this.member = member;
    }
}
