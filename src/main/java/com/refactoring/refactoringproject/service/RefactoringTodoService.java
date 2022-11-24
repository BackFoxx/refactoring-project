package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.*;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.entity.RefactoringTodoOrder;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public static final String SAVING_REFACTORING_TODO_COMPLETED_MESSAGE_FORMAT = "Saving RefactoringTodo Completed. Article ID: {}";
    public static final String REFACTORING_TODO_NOT_FOUND = "you tried to find a RefactoringTodo with NULL id";
    public static final String REFACTORING_TODO_NOT_FOUNT_MESSAGE_FORMAT = "there is no RefactoringTodo with id %d";
    public static final String ID_NULL_MESSAGE = "you tried to find a RefactoringTodo with NULL id";
    public static final String UPDATING_REFACTORING_TODO_NOT_EXISTING = "you tried to update a RefactoringTodo which is not existing";
    public static final String UPDATING_REFACTORING_TODO_WRITTEN_BY_ANOTHER = "you tried to update refactoringTodo written by another user";
    public static final String DELETING_REFACTORING_TODO_COMPLETED_LOG_FORMAT = "Deleting RefactoringTodo Completed. Article ID: {}";
    private final RefactoringTodoRepository refactoringTodoRepository;

    public Long saveRefactoringTodo(RefactoringTodoFormat dto) {
        RefactoringTodo refactoringTodo = RefactoringTodoFormat.toEntity(dto);
        RefactoringTodo savedRefactoringTodo = refactoringTodoRepository.save(refactoringTodo);
        log.info(SAVING_REFACTORING_TODO_COMPLETED_MESSAGE_FORMAT, refactoringTodo.getId());

        return savedRefactoringTodo.getId();
    }

    public RefactoringTodoResponse findOneById(Long savedId) {
        if (savedId == null) {
            throw new IllegalArgumentException(REFACTORING_TODO_NOT_FOUND);
        }

        Optional<RefactoringTodo> resultOptional = refactoringTodoRepository.findById(savedId);
        RefactoringTodo findRefactoringTodo = resultOptional.orElseThrow(() -> new EmptyResultDataAccessException(String.format(REFACTORING_TODO_NOT_FOUNT_MESSAGE_FORMAT, savedId), 1));
        return RefactoringTodoResponse.from(findRefactoringTodo);
    }

    public void deleteOneById(Long savedId) {
        if (savedId == null) {
            throw new IllegalArgumentException(ID_NULL_MESSAGE);
        }

        refactoringTodoRepository.deleteById(savedId);

        log.info(DELETING_REFACTORING_TODO_COMPLETED_LOG_FORMAT, savedId);
    }

    public Page<RefactoringTodoResponse> findList(Pageable pageable) {
        return refactoringTodoRepository.findListWithFavoriteCount(pageable);
    }

    public void updateRefactoringTodo(RefactoringTodoUpdateFormat refactoringTodoUpdateFormat) {
        Optional<RefactoringTodo> targetOptional = refactoringTodoRepository.findById(refactoringTodoUpdateFormat.getRefactoringTodoId());
        if (targetOptional.isEmpty()) {
            throw new IllegalArgumentException(UPDATING_REFACTORING_TODO_NOT_EXISTING);
        } // 수정하려는 RefactoringTodo가 존재하는지 검사
        RefactoringTodo target = targetOptional.get();

        if (!refactoringTodoUpdateFormat.getMember().equals(target.getMember())) {
            throw new IllegalArgumentException(UPDATING_REFACTORING_TODO_WRITTEN_BY_ANOTHER);
        }

        target.changeLanguage(refactoringTodoUpdateFormat.getLanguage());
        target.changeCode(refactoringTodoUpdateFormat.getCode());
        target.changeDescription(refactoringTodoUpdateFormat.getDescription());
        List<RefactoringTodoOrder> orders = refactoringTodoUpdateFormat.getTodoOrderFormat().stream()
                .map(RefactoringTodoOrderFormat::toEntity)
                .collect(Collectors.toList());
        target.changeOrders(orders);
    }
}
