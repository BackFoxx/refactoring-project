package com.refactoring.refactoringproject.repository;

import com.refactoring.refactoringproject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {

}
