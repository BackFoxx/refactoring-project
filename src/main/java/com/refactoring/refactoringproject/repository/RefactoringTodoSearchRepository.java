package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.dto.RefactoringTodoResponse;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefactoringTodoSearchRepository {
    Page<RefactoringTodoResponse> findListWithFavoriteCount(Pageable pageable);

    RefactoringTodo testQuery();
}
