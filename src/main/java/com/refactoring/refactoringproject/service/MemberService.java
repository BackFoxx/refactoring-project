package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.MemberSignInFormat;
import com.refactoring.refactoringproject.entity.Favorite;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.repository.FavoriteRepository;
import com.refactoring.refactoringproject.repository.MemberRepository;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final RefactoringTodoRepository refactoringTodoRepository;
    private final FavoriteRepository favoriteRepository;

    public void signIn(MemberSignInFormat signInFormat) {
        if (memberRepository.findById(signInFormat.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Member already exists with this email: " + signInFormat.getEmail());
        } // TODO: insert문에서 어차피 id 중복여부를 확인할 텐데 select문을 굳이 쏴 id 중복 여부를 확인하고 있다.

        Member member = MemberSignInFormat.toEntity(signInFormat);
        Member savedMember = memberRepository.save(member);
        log.info("Signing In Completed. Member ID: {}", savedMember.getId());
    }

    public void assignFavorite(Long refactoringTodoId, Member member) { //TODO: 스프링 시큐리티 도입 이후에는 파라미터로 꺼낼 필요가 없다.
        RefactoringTodo refactoringTodo = refactoringTodoRepository.findById(refactoringTodoId)
                .orElseThrow(() -> new EmptyResultDataAccessException("there is no RefactoringTodo with id " + refactoringTodoId, 1));

        Favorite favorite = Favorite.of(member, refactoringTodo);
        favoriteRepository.save(favorite);

        member.addFavorite(favorite);
    }
}
