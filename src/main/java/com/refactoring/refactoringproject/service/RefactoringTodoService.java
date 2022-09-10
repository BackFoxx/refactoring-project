package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.RefactoringTodoFormat;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefactoringTodoService {
    private final RefactoringTodoRepository refactoringTodoRepository;

    public void saveRefactoringTodo(RefactoringTodoFormat dto) {
        RefactoringTodo refactoringTodo = RefactoringTodoFormat.toEntity(dto);
        refactoringTodoRepository.save(refactoringTodo);
        log.info("Saving RefactoringTodo Completed. Article ID: {}", refactoringTodo.getId());
    }
}
