package com.refactoring.refactoringproject.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class RefactoringTodoOrder extends BaseEntityTime {
    public RefactoringTodoOrder() {
    }

    public RefactoringTodoOrder(Long id, RefactoringTodo refactoringTodo, String content) {
        this.id = id;
        this.refactoringTodo = refactoringTodo;
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "REFACTORING_TODO_ID")
    private RefactoringTodo refactoringTodo;

    @Column(length = 200, nullable = false)
    private String content;

    public void assignRefactoringTodo(RefactoringTodo refactoringTodo) {
        if (this.refactoringTodo != null) {
            throw new IllegalArgumentException("You can't change RefactoringTodo of RefactoringOrder which is already assigned.");
        }

        this.refactoringTodo = refactoringTodo;
    }
}
