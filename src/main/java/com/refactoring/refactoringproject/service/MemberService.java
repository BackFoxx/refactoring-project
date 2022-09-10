package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.MemberSignInFormat;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public void signIn(MemberSignInFormat signInFormat) {
        if (memberRepository.findById(signInFormat.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Member already exists with this email: " + signInFormat.getEmail());
        }

        Member member = MemberSignInFormat.toEntity(signInFormat);
        Member savedMember = memberRepository.save(member);
        log.info("Signing In Completed. Member ID: {}", savedMember.getId());
    }
}
