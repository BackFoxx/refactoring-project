package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByMemberIdAndRefactoringTodoId(String memberId, Long refactoringTodoId);
}
