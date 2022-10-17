package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.entity.RefactoringDone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefactoringDoneRepository extends JpaRepository<RefactoringDone, Long> {

}
