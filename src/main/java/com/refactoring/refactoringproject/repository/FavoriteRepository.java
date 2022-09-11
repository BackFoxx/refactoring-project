package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
