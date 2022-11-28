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
    public static final String MEMBER_ALREADY_EXISTS_WITH_THIS_EMAIL_MESSAGE_FORMAT = "Member already exists with this email: %s";
    public static final String SIGNING_IN_COMPLETED_LOG_FORMAT = "Signing In Completed. Member ID: {}";
    public static final String ALREADY_ASSIGNED_REFACTORING_TODO_ID_MESSAGE_FORMAT = "member can't assign RefactoringTodo which is already assigned. RefactoringTodo Id: %s";
    public static final String REFACTORING_TODO_NOTFOUNT_MESSAGE_FORMAT = "there is no RefactoringTodo with id %d";
    public static final String INVALID_ASSIGNING_REFACTORING_TODO_MESSAGE_FORMAT = "member can't assign RefactoringTodo of himself to favorite. RefactoringTodo Id: %d";

    private final MemberRepository memberRepository;
    private final RefactoringTodoRepository refactoringTodoRepository;
    private final FavoriteRepository favoriteRepository;

    public void signIn(MemberSignInFormat signInFormat) {
        if (memberRepository.findById(signInFormat.getEmail()).isPresent()) {
            throw new IllegalArgumentException(String.format(MEMBER_ALREADY_EXISTS_WITH_THIS_EMAIL_MESSAGE_FORMAT, signInFormat.getEmail()));
        } // TODO: insert문에서 어차피 id 중복여부를 확인할 텐데 select문을 굳이 쏴 id 중복 여부를 확인하고 있다.

        Member savedMember = memberRepository.save(MemberSignInFormat.toEntity(signInFormat));
        log.info(SIGNING_IN_COMPLETED_LOG_FORMAT, savedMember.getId());
    }

    public void assignFavorite(Long refactoringTodoId, Member member) { //TODO: 스프링 시큐리티 도입 이후에는 파라미터로 꺼낼 필요가 없다.
        if (favoriteRepository.findByMemberIdAndRefactoringTodoId(member.getId(), refactoringTodoId).isPresent()) {
            throw new IllegalArgumentException(String.format(ALREADY_ASSIGNED_REFACTORING_TODO_ID_MESSAGE_FORMAT, refactoringTodoId));
        }

        RefactoringTodo refactoringTodo = refactoringTodoRepository.findById(refactoringTodoId)
                .orElseThrow(() -> new EmptyResultDataAccessException(String.format(REFACTORING_TODO_NOTFOUNT_MESSAGE_FORMAT, refactoringTodoId), 1));

        if (refactoringTodo.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException(String.format(INVALID_ASSIGNING_REFACTORING_TODO_MESSAGE_FORMAT + refactoringTodoId));
        }

        Favorite favorite = Favorite.of(member, refactoringTodo);
        favoriteRepository.save(favorite);

        member.addFavorite(favorite);
    }
}
