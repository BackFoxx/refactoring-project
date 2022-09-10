package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.entity.RefactoringTodo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefactoringTodoRepository extends JpaRepository<RefactoringTodo, Long> {
}
