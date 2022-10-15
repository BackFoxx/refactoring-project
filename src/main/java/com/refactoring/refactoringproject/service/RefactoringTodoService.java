package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.*;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
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
        RefactoringTodo findRefactoringTodo = resultOptional.orElseThrow(() -> new EmptyResultDataAccessException("there is no RefactoringTodo with id " + savedId, 1));
        return RefactoringTodoResponse.from(findRefactoringTodo);
    }

    public void deleteOneById(Long savedId) {
        if (savedId == null) {
            throw new IllegalArgumentException("you tried to find a RefactoringTodo with NULL id");
        }

        refactoringTodoRepository.deleteById(savedId);

        log.info("Deleting RefactoringTodo Completed. Article ID: {}", savedId);
    }

    public Page<RefactoringTodoResponse> findList(Pageable pageable) {
        return refactoringTodoRepository.findListWithFavoriteCount(pageable);
    }

    public void updateRefactoringTodo(RefactoringTodoUpdateFormat refactoringTodoUpdateFormat) {
        Optional<RefactoringTodo> targetOptional = refactoringTodoRepository.findById(refactoringTodoUpdateFormat.getRefactoringTodoId());
        if (targetOptional.isEmpty()) {
            throw new IllegalArgumentException("you tried to update a RefactoringTodo which is not existing");
        }
        RefactoringTodo target = targetOptional.get();

        target.changeLanguage(refactoringTodoUpdateFormat.getLanguage());
        target.changeCode(refactoringTodoUpdateFormat.getCode());
        target.changeDescription(refactoringTodoUpdateFormat.getDescription());
        List<RefactoringTodoOrder> orders = refactoringTodoUpdateFormat.getTodoOrderFormat().stream()
                .map(RefactoringTodoOrderFormat::toEntity)
                .collect(Collectors.toList());
        target.changeOrders(orders);
    }
}
