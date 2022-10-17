package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.RefactoringDoneFormat;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.repository.RefactoringDoneRepository;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefactoringDoneService {
    private final RefactoringDoneRepository refactoringDoneRepository;
    private final RefactoringTodoRepository refactoringTodoRepository;

    public Long saveRefactoringDone(RefactoringDoneFormat format) {
        Optional<RefactoringTodo> refactoringTodoOptional = refactoringTodoRepository.findById(format.getRefactoringTodoId());
        if (refactoringTodoOptional.isEmpty()) throw new IllegalArgumentException();
        RefactoringTodo refactoringTodo = refactoringTodoOptional.get();

        RefactoringDone refactoringDone = RefactoringDoneFormat.toEntity(refactoringTodo, format);

        RefactoringDone savedRefactoringDone = refactoringDoneRepository.save(refactoringDone);
        return savedRefactoringDone.getId();
    }
}
