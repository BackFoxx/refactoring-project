package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.entity.RefactoringTodo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefactoringTodoRepository extends JpaRepository<RefactoringTodo, Long> {
    @Override
    @EntityGraph(attributePaths = "orders")
    Page<RefactoringTodo> findAll(Pageable pageable);
}
