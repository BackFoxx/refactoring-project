package com.refactoring.refactoringproject.service;

import com.refactoring.refactoringproject.dto.RefactoringDoneFormat;
import com.refactoring.refactoringproject.dto.RefactoringDoneResponse;
import com.refactoring.refactoringproject.entity.Liked;
import com.refactoring.refactoringproject.entity.Member;
import com.refactoring.refactoringproject.entity.RefactoringDone;
import com.refactoring.refactoringproject.entity.RefactoringTodo;
import com.refactoring.refactoringproject.repository.LikedRepository;
import com.refactoring.refactoringproject.repository.RefactoringDoneRepository;
import com.refactoring.refactoringproject.repository.RefactoringTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefactoringDoneService {
    private final RefactoringDoneRepository refactoringDoneRepository;
    private final RefactoringTodoRepository refactoringTodoRepository;
    private final LikedRepository likedRepository;

    public Long saveRefactoringDone(RefactoringDoneFormat format) {
        Optional<RefactoringTodo> refactoringTodoOptional = refactoringTodoRepository.findById(format.getRefactoringTodoId());
        if (refactoringTodoOptional.isEmpty()) throw new IllegalArgumentException("you tried to post a RefactoringDone of RefactoringTodo which is not existing");
        RefactoringTodo refactoringTodo = refactoringTodoOptional.get();

        RefactoringDone refactoringDone = RefactoringDoneFormat.toEntity(refactoringTodo, format);

        RefactoringDone savedRefactoringDone = refactoringDoneRepository.save(refactoringDone);
        return savedRefactoringDone.getId();
    }

    public RefactoringDoneResponse findOneById(Long refactoringDoneId) {
        Optional<RefactoringDone> resultOptional = this.refactoringDoneRepository.findById(refactoringDoneId);
        RefactoringDone refactoringDone = resultOptional.orElseThrow(() -> new EmptyResultDataAccessException("there is no RefactoringDone with id " + refactoringDoneId, 1));
        return RefactoringDoneResponse.from(refactoringDone);
    }

    public void assignLike(Member member, Long refactoringDoneId) {
        Optional<RefactoringDone> refactoringDoneOptional = this.refactoringDoneRepository.findById(refactoringDoneId);
        RefactoringDone refactoringDone = refactoringDoneOptional.orElseThrow(() -> new IllegalArgumentException("you tried to post a Like for RefactoringDone which is not existing"));

        if (this.likedRepository.findByMemberAndRefactoringDone(member, refactoringDone).isPresent()) {
            throw new IllegalArgumentException("You can't post like to RefactoringDone you already posted");
        }

        if (refactoringDone.getMember().equals(member)) {
            throw new IllegalArgumentException("You can't post like to RefactoringDone written by yourself");
        }

        Liked liked = new Liked(null, member, refactoringDone);
        likedRepository.save(liked);
    }

    public void deleteLike(Member member, Long refactoringDoneId) {
        Optional<Liked> likeOptional = this.likedRepository.findByMemberAndRefactoringDone_Id(member, refactoringDoneId);
        Liked liked = likeOptional.orElseThrow(() -> new IllegalArgumentException("You may not have posted like on this RefactoringDone"));

        this.likedRepository.delete(liked);
    }
}
