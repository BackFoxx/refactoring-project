package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.entity.Liked;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikedRepository extends JpaRepository<Liked, Long> {
    Optional<Liked> findByMemberAndRefactoringDone(Member member, RefactoringDone refactoringDone);

    Optional<Liked> findByMemberAndRefactoringDone_Id(Member member, Long refactoringDoneId);
}
