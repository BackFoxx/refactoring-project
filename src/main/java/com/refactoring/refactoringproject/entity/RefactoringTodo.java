package com.refactoring.refactoringproject.entity;

import lombok.Getter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class RefactoringTodo extends BaseEntityTime {
    public RefactoringTodo() {
    }

    public RefactoringTodo(Member member, String language, String code, String description) {
        this.member = member;
        this.language = language;
        this.code = code;
        this.description = description;
    }

    public RefactoringTodo(Long id, Member member, String language, String code, String description, RefactoringDone bestPractice, List<RefactoringTodoOrder> orders) {
        this.id = id;
        this.member = member;
        this.language = language;
        this.code = code;
        this.description = description;
        this.bestPractice = bestPractice;
        this.orders = orders;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(length = 10, nullable = false)
    private String language;

    @Column(length = 10000, nullable = false)
    private String code;

    @Column(length = 1000)
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BEST_PRACTICE")
    private RefactoringDone bestPractice;

    @OneToMany(mappedBy = "refactoringTodo", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<RefactoringTodoOrder> orders = new ArrayList<>();

    @OneToMany(mappedBy = "refactoringTodo")
    private List<Favorite> favorites = new ArrayList<>();

    public void changeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("code cannot be null or empty, code : " + code);
        }
        this.code = code;
    }

    public void changeLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            throw new IllegalArgumentException("language cannot be null or empty, language : " + language);
        }
        this.language = language;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeOrders(List<RefactoringTodoOrder> orders) {
        this.orders.clear();
        orders.forEach(order -> this.addRefactoringOrder(order));
    }

    public void addRefactoringOrder(RefactoringTodoOrder order) {
        this.orders.add(order);
        order.assignRefactoringTodo(this);
    }
}
