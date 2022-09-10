package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.RefactoringTodoFormat;
import com.refactoring.refactoringproject.dto.RefactoringTodoOrderResponse;
import com.refactoring.refactoringproject.dto.RefactoringTodoResponse;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefactoringTodoService {
    private final RefactoringTodoRepository refactoringTodoRepository;

    public Long saveRefactoringTodo(RefactoringTodoFormat dto) {
        RefactoringTodo refactoringTodo = RefactoringTodoFormat.toEntity(dto);
        RefactoringTodo savedRefactoringTodo = refactoringTodoRepository.save(refactoringTodo);
        log.info("Saving RefactoringTodo Completed. Article ID: {}", refactoringTodo.getId());

        return savedRefactoringTodo.getId();
    }

    public RefactoringTodoResponse findOneById(Long savedId) {
        if (savedId == null) {
            throw new IllegalArgumentException("you tried to find a RefactoringTodo with NULL id");
        }

        Optional<RefactoringTodo> resultOptional = refactoringTodoRepository.findById(savedId);
        RefactoringTodo findRefactoringTodo = resultOptional.orElseThrow(() -> new IllegalArgumentException("there is no RefactoringTodo with id " + savedId));

        return RefactoringTodoResponse.from(findRefactoringTodo);
    }
}
